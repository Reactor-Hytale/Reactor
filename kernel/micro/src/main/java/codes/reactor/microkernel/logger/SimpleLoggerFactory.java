package codes.reactor.microkernel.logger;

import codes.reactor.kernel.logger.Logger;
import codes.reactor.kernel.logger.LoggerBuilder;
import codes.reactor.kernel.logger.LoggerFactory;
import org.jetbrains.annotations.NotNull;

public class SimpleLoggerFactory implements LoggerFactory {

    private final @NotNull Logger defaultLogger;

    public SimpleLoggerFactory(final @NotNull Logger defaultLogger) {
        this.defaultLogger = defaultLogger;
    }

    @Override
    public @NotNull Logger createLogger(final @NotNull String prefix) {
        return new WrappedLogger(defaultLogger, prefix.endsWith(" ") ? prefix : prefix + " ", "", defaultLogger.getLoggerFormatter());
    }

    @Override
    public @NotNull Logger createLogger(final LoggerBuilder builder) {
        final String prefix = builder.getPrefix();
        final String suffix = builder.getSuffix();
        return new WrappedLogger(
            defaultLogger,
            prefix.endsWith(" ") ? prefix : prefix + " ",
            suffix.startsWith(" ") ? suffix : " " + suffix,
            builder.getFormatter() != null ? builder.getFormatter() : defaultLogger.getLoggerFormatter()
        );
    }

    @Override
    public @NotNull Logger acquire(final @NotNull Object owner) {
        return acquire(owner.getClass());
    }

    @Override
    public @NotNull Logger acquire(final @NotNull Class<?> ownerClass) {
        final String simpleName = ownerClass.getSimpleName();
        final String name = simpleName.isBlank()
            ? ownerClass.getName()
            : simpleName;
        return createLogger(name);
    }
}
