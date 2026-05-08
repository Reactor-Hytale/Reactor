package codes.reactor.microkernel.logger;

import codes.reactor.kernel.logger.Logger;
import codes.reactor.kernel.logger.LoggerFormatter;
import org.jetbrains.annotations.NotNull;

public final class WrappedLogger implements Logger {
    private final String prefix, suffix;
    private final LoggerFormatter formatter;
    private volatile @NotNull Logger logger;

    public WrappedLogger(final @NotNull Logger logger, final String prefix, final String suffix, final LoggerFormatter formatter) {
        this.logger = logger;
        this.prefix = prefix;
        this.suffix = suffix;
        this.formatter = formatter;
    }

    public @NotNull Logger getLogger() {
        return logger;
    }

    public void setLogger(@NotNull final Logger logger) {
        this.logger = logger;
    }

    @Override
    public @NotNull LoggerFormatter getLoggerFormatter() {
        return formatter;
    }

    @Override
    public void debug(final @NotNull String message) {
        logger.debug(prefix + message + suffix);
    }

    @Override
    public void log(final @NotNull String message) {
        logger.log(prefix + message + suffix);
    }

    @Override
    public void info(final @NotNull String message) {
        logger.info(prefix + message + suffix);
    }

    @Override
    public void warn(final @NotNull String message) {
        logger.warn(prefix + message + suffix);
    }

    @Override
    public void error(final @NotNull String message) {
        logger.error(prefix + message + suffix);
    }

    @Override
    public void error(final @NotNull String message, final @NotNull Throwable throwable) {
        logger.error(prefix + message + suffix, throwable);
    }
}
