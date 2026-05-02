package codes.reactor.microkernel.logger

import codes.reactor.kernel.logger.Logger

class PrintlnLogger : Logger {
    override val loggerFormatter: JavaLoggerFormatter = JavaLoggerFormatter()

    override fun debug(message: String) {
        println("[DEBUG] $message")
    }

    override fun log(message: String) {
        println("[LOG] $message")
    }

    override fun info(message: String) {
        println("[INFO] $message")
    }

    override fun warn(message: String) {
        println("[WARN] $message")
    }

    override fun error(message: String) {
        println("[ERROR] $message")
    }

    override fun error(message: String, throwable: Throwable) {
        System.err.println("[ERROR] $message")
        throwable.printStackTrace(System.err)
    }
}
