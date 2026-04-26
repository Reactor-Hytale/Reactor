package ink.reactor.launcher.logger.type

import ink.reactor.kernel.logger.Logger
import ink.reactor.kernel.logger.LoggerFormatter
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ReactorLogger(
    override val loggerFormatter: LoggerFormatter,
    private val consoleLogger: ConsoleLogger?,
    private val fileLogger: FileLogger?,
    private val debugPrefix: String,
    private val logPrefix: String,
    private val infoPrefix: String,
    private val warnPrefix: String,
    private val errorPrefix: String,
    private val dateTimeFormatter: DateTimeFormatter?,
) : Logger {

    companion object {
        private val ZONE_ID: ZoneId = Clock.systemDefaultZone().zone
    }

    override fun debug(message: String) {
        val prefix: String = formatTime(debugPrefix)
        consoleLogger?.debug(prefix, message)
        fileLogger?.debug(message)
    }

    override fun log(message: String) {
        val prefix: String = formatTime(logPrefix)
        consoleLogger?.log(prefix, message)
        fileLogger?.log(prefix + message)
    }

    override fun info(message: String) {
        val prefix: String = formatTime(infoPrefix)
        consoleLogger?.info(prefix, message)
        fileLogger?.info(prefix + message)
    }

    override fun warn(message: String) {
        val prefix: String = formatTime(warnPrefix)
        consoleLogger?.warn(prefix, message)
        fileLogger?.warn(prefix + message)
    }

    override fun error(message: String) {
        val prefix: String = formatTime(errorPrefix)
        consoleLogger?.error(prefix, message)
        fileLogger?.error(prefix + message)
    }

    override fun error(message: String, throwable: Throwable) {
        val prefix: String = formatTime(errorPrefix)
        consoleLogger?.error(prefix, message, throwable)
        fileLogger?.error(prefix + message, throwable)
    }

    private fun formatTime(prefix: String): String {
        val formatter = dateTimeFormatter ?: return prefix
        val time = LocalDateTime.now(ZONE_ID).format(formatter)
        return prefix.replace("%time%", time)
    }
}
