package ink.reactor.launcher.logger.type

import ink.reactor.kernel.logger.LogLevel
import ink.reactor.kernel.logger.Logger
import ink.reactor.kernel.logger.LoggerFormatter
import ink.reactor.launcher.logger.LogRingBuffer
import ink.reactor.launcher.logger.console.ConsoleAppender
import ink.reactor.launcher.logger.file.FileAppender
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ReactorLogger(
    override val loggerFormatter: LoggerFormatter,
    private val ringBuffer: LogRingBuffer,
    private val debugPrefix: String,
    private val logPrefix: String,
    private val infoPrefix: String,
    private val warnPrefix: String,
    private val errorPrefix: String,
) : Logger {

    override fun debug(message: String) {
        ringBuffer.push(LogLevel.DEBUG, debugPrefix, message, null)
    }

    override fun log(message: String) {
        ringBuffer.push(LogLevel.LOG, logPrefix, message, null)
    }

    override fun info(message: String) {
        ringBuffer.push(LogLevel.INFO, infoPrefix, message, null)
    }

    override fun warn(message: String) {
        ringBuffer.push(LogLevel.WARN, warnPrefix, message, null)
    }

    override fun error(message: String) {
        ringBuffer.push(LogLevel.ERROR, errorPrefix, message, null)
    }

    override fun error(message: String, throwable: Throwable) {
        ringBuffer.push(LogLevel.ERROR, errorPrefix, message, throwable)
    }
}
