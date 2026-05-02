package ink.reactor.launcher.logger.file;

import ink.reactor.launcher.logger.LogAppender;
import ink.reactor.launcher.logger.MutableLogEntry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("NonAtomicOperationOnVolatileField")
public final class FileAppender implements LogAppender {
    private static final byte NEW_LINE = (byte) '\n';
    private static final byte TAB = (byte) '\t';

    private final long maxFileLength;
    private final FileChannel channel;
    private final ByteBuffer internalBuffer;

    private volatile boolean closed = false;

    private volatile long currentFileLength = 0L;

    public boolean canWrite() {
        return !closed && channel.isOpen() && currentFileLength < maxFileLength;
    }

    public FileAppender(long maxFileLength, int bufferSize, FileChannel channel) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }

        this.maxFileLength = maxFileLength;
        this.channel = channel;
        this.internalBuffer = ByteBuffer.allocateDirect(bufferSize);
    }

    @Override
    public void append(final MutableLogEntry entry) throws IOException {
        if (!canWrite()) {
            return;
        }

        if (entry.prefix != null) appendString(entry.prefix);
        if (entry.message != null) appendString(entry.message);
        appendByte(NEW_LINE);

        if (entry.throwable != null) {
            writeThrowable(entry.throwable);
        }
    }

    private void appendBytes(byte[] data) throws IOException {
        if (data.length == 0) {
            return;
        }

        if (data.length > internalBuffer.capacity()) {
            flushToDisk();
            writeFully(ByteBuffer.wrap(data));
            currentFileLength += data.length;
            return;
        }

        ensureWritable(data.length);
        internalBuffer.put(data);
        currentFileLength += data.length;
    }

    private void writeFully(ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    public void flushToDisk() throws IOException {
        if (internalBuffer.position() == 0) {
            return;
        }

        internalBuffer.flip();
        try {
            while (internalBuffer.hasRemaining()) {
                channel.write(internalBuffer);
            }
        } finally {
            internalBuffer.clear();
        }
    }

    private void appendString(String data) throws IOException {
        if (data == null || data.isEmpty()) {
            return;
        }

        final int len = data.length();
        final long worstCaseBytes = (long) len * 4L;

        if (worstCaseBytes > internalBuffer.capacity()) {
            appendBytes(data.getBytes(StandardCharsets.UTF_8));
            return;
        }

        ensureWritable((int) worstCaseBytes);
        final int startPos = internalBuffer.position();

        for (int i = 0; i < len; i++) {
            final char c = data.charAt(i);

            if (c < 0x80) {
                internalBuffer.put((byte) c);
            } else if (c < 0x800) {
                internalBuffer.put((byte) (0xC0 | (c >> 6)));
                internalBuffer.put((byte) (0x80 | (c & 0x3F)));
            } else if (
                Character.isHighSurrogate(c)
                    && i + 1 < len
                    && Character.isLowSurrogate(data.charAt(i + 1))
            ) {
                final int codePoint = Character.toCodePoint(c, data.charAt(++i));
                internalBuffer.put((byte) (0xF0 | (codePoint >> 18)));
                internalBuffer.put((byte) (0x80 | ((codePoint >> 12) & 0x3F)));
                internalBuffer.put((byte) (0x80 | ((codePoint >> 6) & 0x3F)));
                internalBuffer.put((byte) (0x80 | (codePoint & 0x3F)));
            } else if (Character.isSurrogate(c)) {
                // Replacement character U+FFFD para surrogates inválidos.
                internalBuffer.put((byte) 0xEF);
                internalBuffer.put((byte) 0xBF);
                internalBuffer.put((byte) 0xBD);
            } else {
                internalBuffer.put((byte) (0xE0 | (c >> 12)));
                internalBuffer.put((byte) (0x80 | ((c >> 6) & 0x3F)));
                internalBuffer.put((byte) (0x80 | (c & 0x3F)));
            }
        }

        currentFileLength += internalBuffer.position() - startPos;
    }

    private void writeThrowable(Throwable throwable) throws IOException {
        Throwable current = throwable;
        boolean causedBy = false;

        while (current != null) {
            if (causedBy) {
                appendString("Caused by: ");
            }

            appendString(current.toString());
            appendByte(NEW_LINE);

            for (final StackTraceElement element : current.getStackTrace()) {
                appendByte(TAB);
                appendString("at ");
                appendString(element.toString());
                appendByte(NEW_LINE);
            }

            current = current.getCause();
            causedBy = true;
        }
    }

    private void appendByte(byte value) throws IOException {
        ensureWritable(1);
        internalBuffer.put(value);
        currentFileLength++;
    }

    private void ensureWritable(int bytes) throws IOException {
        if (bytes > internalBuffer.remaining()) {
            flushToDisk();
        }
    }

    @Override
    public void flush() throws IOException {
        if (internalBuffer.position() == 0) return;
        internalBuffer.flip();
        while (internalBuffer.hasRemaining()) {
            channel.write(internalBuffer);
        }
        internalBuffer.clear();
    }

    @Override
    public void close() throws Exception {
        if (closed) {
            return;
        }

        closed = true;
        flush();
        channel.close();
    }
}
