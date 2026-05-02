package ink.reactor.launcher.logger.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public final class FileWriter {
    private final long maxFileLength;
    private final FileChannel channel;
    private final ByteBuffer internalBuffer;

    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(2048);
    private long currentFileLength;

    public FileWriter(long maxFileLength, int bufferSize, FileChannel channel) {
        this.maxFileLength = maxFileLength;
        this.channel = channel;
        this.internalBuffer = ByteBuffer.allocateDirect(bufferSize);
    }

    public BlockingQueue<String> getQueue() {
        return queue;
    }

    public boolean canWrite() {
        return currentFileLength < maxFileLength && channel.isOpen();
    }

    public void write(final String message) {
        if (message == null || !canWrite()) {
            return;
        }
        if (!this.queue.offer(message)) {
            System.err.println("[File Logger] Queue full, dropping log message");
        }
    }

    public void processNextBlocking(final long timeoutNanos) {
        try {
            final Object firstData = queue.poll(timeoutNanos, TimeUnit.NANOSECONDS);

            if (firstData == null) {
                flushToDisk();
                return;
            }

            processData(firstData);

            Object data;
            while ((data = queue.poll()) != null) {
                processData(data);
            }

            flushToDisk();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.println("Critical error writing to log file");
            e.printStackTrace(System.err);
        }
    }

    private void processData(Object data) throws IOException {
        if (data instanceof byte[]) {
            processBytes((byte[]) data);
        } else if (data instanceof String) {
            processString((String) data);
        }
    }

    private void processBytes(byte[] data) throws IOException {
        if (data.length + 1 > internalBuffer.remaining()) {
            flushToDisk();
        }

        if (data.length + 1 > internalBuffer.capacity()) {
            channel.write(ByteBuffer.wrap(data));
            channel.write(ByteBuffer.wrap(new byte[]{'\n'}));
            currentFileLength += data.length + 1;
            return;
        }

        internalBuffer.put(data);
        internalBuffer.put((byte) '\n');
        currentFileLength += data.length + 1;
    }

    /**
     * Processes a String message and writes it to the internal buffer using manual UTF-8 encoding.
     * <p>
     * This method is designed for <b>Zero-Allocation logging</b>. Unlike {@link String#getBytes()},
     * it translates characters directly into the {@link ByteBuffer} without creating intermediate
     * byte array objects, significantly reducing Garbage Collector (GC) pressure.
     * </p>
     *
     * <p>The encoding logic follows the UTF-8 specification:</p>
     * <ul>
     *     <li><b>1 Byte:</b> Standard ASCII characters (U+0000 to U+007F).</li>
     *     <li><b>2 Bytes:</b> Latin-script alphabets with diacritics, Greek, Cyrillic, etc. (U+0080 to U+07FF).</li>
     *     <li><b>3 Bytes:</b> Most common CJK characters and other symbols (U+0800 to U+FFFF).</li>
     *     <li><b>4 Bytes:</b> Supplementary planes, including Emojis (Surrogate pairs).</li>
     * </ul>
     *
     * @param data The string message to be logged.
     * @throws IOException If an I/O error occurs during buffer flushing to disk.
     */
    private void processString(String data) throws IOException {
        final int len = data.length();

        /*
         * Safety Check: In UTF-8, one character can take up to 4 bytes.
         * We ensure the buffer has enough space for the worst-case scenario (+1 for the newline).
         */
        if (len * 4 + 1 > internalBuffer.remaining()) {
            flushToDisk();
        }

        // Edge Case: If the string is larger than the total buffer capacity, we fall back to standard heap allocation to prevent BufferOverflow.
        if (len * 4 + 1 > internalBuffer.capacity()) {
            processBytes(data.getBytes(StandardCharsets.UTF_8));
            return;
        }

        final int startPos = internalBuffer.position();

        /*
         * Manual UTF-8 Encoding Loop:
         * Iterates through the string and applies bitwise operations to convert
         * Java's UTF-16 chars into UTF-8 bytes directly in the Direct ByteBuffer.
         */
        for (int i = 0; i < len; i++) {
            char c = data.charAt(i);

            if (c < 0x80) {
                // 1-byte sequence (ASCII)
                internalBuffer.put((byte) c);
            } else if (c < 0x800) {
                // 2-byte sequence
                internalBuffer.put((byte) (0xC0 | (c >> 6)));
                internalBuffer.put((byte) (0x80 | (c & 0x3F)));
            } else if (Character.isSurrogate(c)) {
                // 4-byte sequence (Emojis/Supplementary characters)
                // Character.codePointAt handles the surrogate pair conversion
                int codePoint = Character.codePointAt(data, i);
                i++; // Increment index to skip the low surrogate
                internalBuffer.put((byte) (0xF0 | (codePoint >> 18)));
                internalBuffer.put((byte) (0x80 | ((codePoint >> 12) & 0x3F)));
                internalBuffer.put((byte) (0x80 | ((codePoint >> 6) & 0x3F)));
                internalBuffer.put((byte) (0x80 | (codePoint & 0x3F)));
            } else {
                // 3-byte sequence
                internalBuffer.put((byte) (0xE0 | (c >> 12)));
                internalBuffer.put((byte) (0x80 | ((c >> 6) & 0x3F)));
                internalBuffer.put((byte) (0x80 | (c & 0x3F)));
            }
        }

        internalBuffer.put((byte) '\n');

        // Update the total file length tracker based on actual bytes consumed in the buffer
        currentFileLength += (internalBuffer.position() - startPos);
    }

    private void flushToDisk() throws IOException {
        if (internalBuffer.position() == 0) {
            return;
        }

        internalBuffer.flip();
        channel.write(internalBuffer);
        internalBuffer.clear();
    }

    public void close() {
        Object data;
        while ((data = queue.poll()) != null) {
            try {
                processData(data);
            } catch (IOException e) {
                System.err.println("Error processing remaining logs during close");
            }
        }

        try {
            flushToDisk();
            if (channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            System.err.println("Error on close file channel");
            e.printStackTrace(System.err);
        }
    }
}
