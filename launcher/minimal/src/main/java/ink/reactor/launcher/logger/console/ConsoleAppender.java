package ink.reactor.launcher.logger.console;

import ink.reactor.launcher.logger.ConsoleStyle;
import ink.reactor.launcher.logger.LogAppender;
import ink.reactor.launcher.logger.LoggerLevels;
import ink.reactor.launcher.logger.MutableLogEntry;

import java.io.PrintWriter;

public final class ConsoleAppender implements LogAppender {
    private final PrintWriter writer;
    private final LoggerLevels loggerLevels;
    private final ConsoleStyle debugStyle, logStyle, infoStyle, warnStyle, errorStyle;

    public ConsoleAppender(final PrintWriter writer, final LoggerLevels loggerLevels, final ConsoleStyle debugStyle, final ConsoleStyle logStyle, final ConsoleStyle infoStyle, final ConsoleStyle warnStyle, final ConsoleStyle errorStyle) {
        this.writer = writer;
        this.loggerLevels = loggerLevels;
        this.debugStyle = debugStyle;
        this.logStyle = logStyle;
        this.infoStyle = infoStyle;
        this.warnStyle = warnStyle;
        this.errorStyle = errorStyle;
    }

    @Override
    public void append(MutableLogEntry entry) {
        final ConsoleStyle style = switch (entry.level) {
            case DEBUG -> loggerLevels.getDebug() ? debugStyle : null;
            case INFO -> loggerLevels.getInfo() ? infoStyle : null;
            case LOG -> loggerLevels.getLog() ? logStyle : null;
            case WARN -> loggerLevels.getWarn() ? warnStyle : null;
            case ERROR -> loggerLevels.getError() ? errorStyle : null;
        };

        if (style == null) {
            return;
        }
        writer
            .append('\n')
            .append(style.getPrefix())
            .append(entry.prefix)
            .append(style.getText())
            .append(entry.message);

        if (entry.throwable != null) {
            writer.append('\n');
            entry.throwable.printStackTrace(writer);
        }
        writer.append(style.getAfterText());

        writer.flush();
    }

    @Override
    public void flush() {
        writer.flush();
    }

    @Override
    public void close() {}
}
