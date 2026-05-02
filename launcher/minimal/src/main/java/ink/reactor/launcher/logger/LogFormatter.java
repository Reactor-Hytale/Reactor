package codes.reactor.launcher.logger;

import org.jetbrains.annotations.Nullable;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class LogFormatter {
    private final @Nullable DateTimeFormatter dateTimeFormatter;
    private static final ZoneId ZONE_ID = Clock.systemDefaultZone().getZone();

    public LogFormatter(final @Nullable DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public void format(final MutableLogEntry logEntry) {
        if (dateTimeFormatter == null) {
            return;
        }
        logEntry.prefix = logEntry.prefix.replace("%time%", LocalDateTime.now(ZONE_ID).format(dateTimeFormatter));
    }
}
