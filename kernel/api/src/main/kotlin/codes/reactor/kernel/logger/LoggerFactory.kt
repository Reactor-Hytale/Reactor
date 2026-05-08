package codes.reactor.kernel.logger

interface LoggerFactory {
    fun createLogger(name: String): Logger
    fun createLogger(builder: LoggerBuilder): Logger

    fun acquire(any: Any): Logger
    fun acquire(clazz: Class<*>): Logger
}
