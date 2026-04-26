package ink.reactor.kernel.logger

interface Logger {
    val loggerFormatter: LoggerFormatter

    fun debug(message: String)
    fun log(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
    fun error(message: String, throwable: Throwable)

    fun debug(message: String, vararg toFormat: Any?) {
        debug(this.loggerFormatter.format(message, *toFormat))
    }
    fun log(message: String, vararg toFormat: Any?) {
        log(this.loggerFormatter.format(message, *toFormat))
    }
    fun info(message: String, vararg toFormat: Any?) {
        info(this.loggerFormatter.format(message, *toFormat))
    }
    fun warn(message: String, vararg toFormat: Any?) {
        warn(this.loggerFormatter.format(message, *toFormat))
    }
    fun error(message: String, vararg toFormat: Any?) {
        error(this.loggerFormatter.format(message, *toFormat))
    }
    fun error(message: String, throwable: Throwable, vararg toFormat: Any?) {
        error(this.loggerFormatter.format(message, *toFormat), throwable)
    }

    fun log(level: LogLevel, message: String) {
        when (level) {
            LogLevel.LOG -> log(message)
            LogLevel.DEBUG -> debug(message)
            LogLevel.ERROR -> error(message)
            LogLevel.WARN -> warn(message)
            LogLevel.INFO -> info(message)
        }
    }
}
