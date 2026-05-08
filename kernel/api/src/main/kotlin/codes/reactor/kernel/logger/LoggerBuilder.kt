package codes.reactor.kernel.logger

import codes.reactor.kernel.Reactor

class LoggerBuilder(
    private val factory: LoggerFactory = Reactor.loggerFactory
) {

    var prefix: String = ""
    var suffix: String = ""
    var formatter: LoggerFormatter? = null

    fun withPrefix(prefix: String): LoggerBuilder {
        this.prefix = prefix
        return this
    }

    fun withSuffix(suffix: String): LoggerBuilder {
        this.suffix = suffix
        return this
    }

    fun withFormatter(formatter: LoggerFormatter): LoggerBuilder {
        this.formatter = formatter
        return this
    }

    fun build(): Logger {
        return factory.createLogger(this)
    }
}
