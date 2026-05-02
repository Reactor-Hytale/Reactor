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
        switch (entry.level) {
            case DEBUG -> { if (loggerLevels.getDebug()) append(entry, debugStyle); }
            case INFO  -> { if (loggerLevels.getInfo()) append(entry, infoStyle); }
            case LOG   -> { if (loggerLevels.getDebug()) append(entry, logStyle); }
            case WARN  -> { if (loggerLevels.getWarn()) append(entry, warnStyle); }
            case ERROR -> { if (loggerLevels.getError()) append(entry, errorStyle); }
        }

        if (entry.throwable != null) {
            entry.throwable.printStackTrace(writer);
        }
        writer.flush();
    }

    private void append(final MutableLogEntry entry, final ConsoleStyle style) {
        writer
            .append('\n')
            .append(style.getPrefix())
            .append(entry.prefix)
            .append(style.getText())
            .append(entry.message)
            .append(style.getAfterText());
    }

    @Override
    public void flush() {
        writer.flush();
    }

    @Override
    public void close() {}
}
