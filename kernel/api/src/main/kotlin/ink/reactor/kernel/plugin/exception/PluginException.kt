package ink.reactor.kernel.plugin.exception

open class PluginException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
