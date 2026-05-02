package codes.reactor.kernel.plugin.exception

import codes.reactor.kernel.plugin.model.PluginId

class PluginNotFoundException(
    id: PluginId,
    cause: Throwable? = null
) : PluginException(id.value, cause)
