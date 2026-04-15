package ink.reactor.kernel.plugin.exception

import ink.reactor.kernel.plugin.model.PluginId

class PluginNotFoundException(
    id: PluginId,
    cause: Throwable? = null
) : PluginException(id.value, cause)
