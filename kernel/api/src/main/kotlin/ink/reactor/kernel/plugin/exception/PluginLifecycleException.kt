package ink.reactor.kernel.plugin.exception

open class PluginLifecycleException(
    message: String,
    cause: Throwable? = null
) : PluginException(message, cause)
