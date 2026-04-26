package ink.reactor.kernel.plugin.exception

class PluginDescriptorException(
    message: String,
    cause: Throwable? = null
) : PluginException(message, cause)
