package ink.reactor.launcher.logger.file;

import java.util.concurrent.TimeUnit;

public final class FileLogProcessorThread extends Thread {
    private final FileWriter fileWriter;
    private final long intervalNanos;
    private volatile boolean running = true;

    public FileLogProcessorThread(FileWriter fileWriter, long intervalSeconds) {
        super("Log-Processor-Thread");
        this.fileWriter = fileWriter;
        this.intervalNanos = TimeUnit.SECONDS.toNanos(intervalSeconds <= 0 ? 1 : intervalSeconds);
        this.setDaemon(true);
    }

    @Override
    public void run() {
        while (running || !fileWriter.getQueue().isEmpty()) {
            fileWriter.processNextBlocking(intervalNanos);
        }
    }

    public void shutdown() {
        this.running = false;
        this.interrupt();
    }
}
