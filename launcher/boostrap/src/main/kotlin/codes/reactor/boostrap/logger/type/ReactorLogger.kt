package codes.reactor.boostrap.logger.type

import codes.reactor.kernel.Reactor
import codes.reactor.kernel.logger.LogLevel
import codes.reactor.kernel.logger.Logger
import codes.reactor.kernel.logger.LoggerFormatter
import codes.reactor.boostrap.logger.LogRingBuffer
import codes.reactor.microkernel.plugin.scope.provider.ScopedLoggerSpy

class ReactorLogger(
    override val loggerFormatter: LoggerFormatter,
    private val ringBuffer: LogRingBuffer,
    private val debugPrefix: String,
    private val logPrefix: String,
    private val infoPrefix: String,
    private val warnPrefix: String,
    private val errorPrefix: String,
) : Logger {

    private fun getSpyLoggers(): Set<Logger> {
        val spy = Reactor.loggerSpy as ScopedLoggerSpy
        return spy.getLoggers()
    }

    override fun debug(message: String) {
        ringBuffer.push(LogLevel.DEBUG, debugPrefix, message, null)

        getSpyLoggers().forEach { it.debug(message) }
    }

    override fun log(message: String) {
        ringBuffer.push(LogLevel.LOG, logPrefix, message, null)

        getSpyLoggers().forEach { it.log(message) }
    }

    override fun info(message: String) {
        ringBuffer.push(LogLevel.INFO, infoPrefix, message, null)

        getSpyLoggers().forEach { it.info(message) }
    }

    override fun warn(message: String) {
        ringBuffer.push(LogLevel.WARN, warnPrefix, message, null)

        getSpyLoggers().forEach { it.warn(message) }
    }

    override fun error(message: String) {
        ringBuffer.push(LogLevel.ERROR, errorPrefix, message, null)

        getSpyLoggers().forEach { it.error(message) }
    }

    override fun error(message: String, throwable: Throwable) {
        ringBuffer.push(LogLevel.ERROR, errorPrefix, message, throwable)

        getSpyLoggers().forEach { it.error(message, throwable) }
    }
}
