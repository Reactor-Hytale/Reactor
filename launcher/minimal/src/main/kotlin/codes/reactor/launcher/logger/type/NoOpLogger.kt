package codes.reactor.launcher.logger.type

import codes.reactor.kernel.logger.Logger
import codes.reactor.microkernel.logger.JavaLoggerFormatter

class NoOpLogger(
    override val loggerFormatter: JavaLoggerFormatter = JavaLoggerFormatter()
) : Logger {
    override fun debug(message: String) {}
    override fun log(message: String) {}
    override fun info(message: String) {}
    override fun warn(message: String) {}
    override fun error(message: String) {}
    override fun error(message: String, throwable: Throwable) {}
}
