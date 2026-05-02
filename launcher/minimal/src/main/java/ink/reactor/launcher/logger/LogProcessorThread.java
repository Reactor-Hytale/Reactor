package codes.reactor.launcher.logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public final class LogProcessorThread extends Thread {
    private final @NotNull LogRingBuffer ringBuffer;
    private final long intervalNanos;

    private final @NotNull LogFormatter logFormatter;
    private final @Nullable LogAppender consoleAppender;
    private final @Nullable LogAppender fileAppender;

    private volatile boolean running = true;

    public LogProcessorThread(@NotNull LogRingBuffer ringBuffer, long intervalSeconds, final @Nullable LogAppender consoleAppender, final @Nullable LogAppender fileAppender, final @NotNull LogFormatter logFormatter) {
        super("Log-Processor-Thread");
        this.ringBuffer = ringBuffer;
        this.intervalNanos = TimeUnit.SECONDS.toNanos(intervalSeconds <= 0 ? 1 : intervalSeconds);
        this.consoleAppender = consoleAppender;
        this.fileAppender = fileAppender;
        this.logFormatter = logFormatter;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        long nextFlushAt = System.nanoTime() + intervalNanos;

        while (running || !ringBuffer.isEmpty()) {
            int processed = 0;
            MutableLogEntry entry;

            while ((entry = ringBuffer.poll()) != null) {
                try {
                    logFormatter.format(entry);
                    handleEntry(entry);
                } catch (Exception e) {
                    System.err.println("[LogProcessor] Error appending log entry");
                    e.printStackTrace(System.err);
                } finally {
                    ringBuffer.release(entry);
                }
                processed++;
            }

            final long now = System.nanoTime();
            if (now >= nextFlushAt) {
                flushAppenders();
                nextFlushAt = now + intervalNanos;
            }

            if (!running) break;

            if (processed == 0) {
                long waitNanos = nextFlushAt - System.nanoTime();
                if (waitNanos > 0) {
                    LockSupport.parkNanos(this, waitNanos);
                }
            }
        }

        flushAppenders();
        closeAppenders();
    }

    private void handleEntry(MutableLogEntry entry) throws IOException {
        if (consoleAppender != null) {
            consoleAppender.append(entry);
        }
        if (fileAppender != null) {
            fileAppender.append(entry);
        }
    }

    private void flushAppenders() {
        try { if (fileAppender != null) fileAppender.flush(); } catch (Exception ignored) {}
        try { if (consoleAppender != null) consoleAppender.flush(); } catch (Exception ignored) {}
    }

    private void closeAppenders() {
        try { if (fileAppender != null) fileAppender.close(); } catch (Exception ignored) {}
        try { if (consoleAppender != null) consoleAppender.close(); } catch (Exception ignored) {}
    }

    public void shutdown() {
        this.running = false;
        LockSupport.unpark(this);
    }
}
