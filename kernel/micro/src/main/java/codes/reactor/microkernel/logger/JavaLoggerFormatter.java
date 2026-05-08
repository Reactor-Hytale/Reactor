package codes.reactor.microkernel.logger;

import codes.reactor.kernel.logger.LoggerFormatter;
import org.jetbrains.annotations.NotNull;

import java.util.Formatter;

public final class JavaLoggerFormatter implements LoggerFormatter {
    private final Formatter formatter;

    public JavaLoggerFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public JavaLoggerFormatter() {
        this.formatter = new Formatter();
    }

    @Override
    public @NotNull String format(final @NotNull String text, final Object... objects) {
        return (objects == null || objects.length == 0) ? text : formatter.format(text, objects).toString();
    }
}
