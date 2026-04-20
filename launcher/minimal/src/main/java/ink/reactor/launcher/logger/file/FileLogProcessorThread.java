package ink.reactor.launcher.logger.file;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public final class FileLogProcessorThread extends Thread {
    private final FileWriter fileWriter;
    private final long intervalNanos;
    private volatile boolean running = true;

    public FileLogProcessorThread(FileWriter fileWriter, long intervalSeconds) {
        super("Log-Processor-Thread");
        this.fileWriter = fileWriter;
        this.intervalNanos = TimeUnit.SECONDS.toNanos(intervalSeconds);
        this.setName("File-log processor thread");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        while (running || !fileWriter.getQueue().isEmpty()) {
            fileWriter.processQueue();
            if (running) {
                LockSupport.parkNanos(intervalNanos);
            }
        }
    }

    public void shutdown() {
        this.running = false;
        LockSupport.unpark(this);
    }
}
