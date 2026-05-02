package codes.reactor.microkernel.logger.plugin;

import codes.reactor.kernel.Reactor;
import codes.reactor.kernel.logger.LogLevel;
import codes.reactor.kernel.logger.Logger;
import codes.reactor.kernel.logger.LoggerFormatter;
import codes.reactor.microkernel.Microkernel;
import codes.reactor.microkernel.config.KernelPluginConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

public final class PluginStartupLogger implements Logger {

    private static class LogEntry {
        private final LogLevel level;
        private final String message;

        public LogEntry(LogLevel level, String message) {
            this.level = level;
            this.message = message;
        }
    }

    private final int maxLines;
    private final Object lock = new Object();

    private final Logger initialLogger;
    private final ArrayDeque<LogEntry> entries;
    private final KernelPluginConfig.Logging.Strategy strategy;

    private int skippedLines = 0;

    public PluginStartupLogger(final Logger initialLogger) {
        final KernelPluginConfig.Logging logging = Microkernel.Companion.getInstance().getPluginConfig().getLogging();
        this.entries = new ArrayDeque<>(logging.getBufferInitialCapacity());
        this.maxLines = logging.getBufferMaxLines();
        this.strategy = logging.getOverFlowStrategy();
        this.initialLogger = initialLogger;
    }

    @Override
    public @NotNull LoggerFormatter getLoggerFormatter() {
        return Microkernel.Companion.getInstance().getRootLogger().getLoggerFormatter();
    }

    public Logger getInitialLogger() {
        return initialLogger;
    }

    @Override
    public void debug(final @NotNull String message) {
        insertLine(message, LogLevel.DEBUG);
    }

    @Override
    public void log(final @NotNull String message) {
        insertLine(message, LogLevel.LOG);
    }

    @Override
    public void info(final @NotNull String message) {
        insertLine(message, LogLevel.INFO);
    }

    @Override
    public void warn(final @NotNull String message) {
        insertLine(message, LogLevel.WARN);
    }

    @Override
    public void error(final @NotNull String message) {
        insertLine(message, LogLevel.ERROR);
    }

    @Override
    public void error(final @NotNull String message, final @NotNull Throwable throwable) {
        synchronized (lock) {
            insertLineInternal(message, LogLevel.ERROR);
            addStacktraceRecursive(throwable, false);
        }
    }

    @Override
    public void debug(final @NotNull String message, final Object... toFormat) {
        debug(getLoggerFormatter().format(message, toFormat));
    }

    @Override
    public void log(final @NotNull String message, final Object... toFormat) {
        log(getLoggerFormatter().format(message, toFormat));
    }

    @Override
    public void info(final @NotNull String message, final Object... toFormat) {
        info(getLoggerFormatter().format(message, toFormat));
    }

    @Override
    public void warn(final @NotNull String message, final Object... toFormat) {
        warn(getLoggerFormatter().format(message, toFormat));
    }

    @Override
    public void error(final @NotNull String message, final Object... toFormat) {
        error(getLoggerFormatter().format(message, toFormat));
    }

    @Override
    public void error(final @NotNull String message, final @NotNull Throwable throwable, final Object... toFormat) {
        error(getLoggerFormatter().format(message, toFormat), throwable);
    }

    @Override
    public void log(final @NotNull LogLevel level, final @NotNull String message) {
        insertLine(message, level);
    }

    private void addStacktraceRecursive(final Throwable throwable, final boolean isCause) {
        String prefix = isCause ? "Caused by: " : "";
        insertLineInternal(prefix + throwable, LogLevel.ERROR);

        for (StackTraceElement element : throwable.getStackTrace()) {
            insertLineInternal("\tat " + element, LogLevel.ERROR);
        }

        Throwable cause = throwable.getCause();
        if (cause != null) {
            addStacktraceRecursive(cause, true);
        }
    }

    private void insertLine(final String line, final LogLevel level) {
        if (line == null) {
            return;
        }
        synchronized (lock) {
            insertLineInternal(line, level);
        }
    }

    private void insertLineInternal(final String line, final LogLevel level) {
        if (entries.size() < maxLines) {
            entries.add(new LogEntry(level, line));
            return;
        }

        switch (strategy) {
            case DROP_NEWEST -> skippedLines++;
            case DROP_OLDEST -> {
                skippedLines++;
                entries.removeFirst();
                entries.addLast(new LogEntry(level, line));
            }
            case FLUSH -> {
                flushUnsafe();
                entries.add(new LogEntry(level, line));
            }
        }
    }

    public void flush() {
        synchronized (lock) {
            flushUnsafe();
        }
    }

    private void flushUnsafe() {
        for (LogEntry entry : entries) {
            initialLogger.log(entry.level, entry.message);
        }

        if (skippedLines > 0) {
            initialLogger.warn("Skipped " + skippedLines + " lines. Max: " + maxLines + ". Using the strategy: " + strategy.name());
        }

        skippedLines = 0;
        entries.clear();
    }
}
