package ink.reactor.kernel.plugin.exception

class PluginLoadException(
    message: String,
    cause: Throwable? = null
) : PluginLifecycleException(message, cause)
