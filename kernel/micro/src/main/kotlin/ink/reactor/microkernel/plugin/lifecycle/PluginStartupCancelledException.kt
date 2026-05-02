package codes.reactor.microkernel.plugin.lifecycle

import codes.reactor.kernel.plugin.model.PluginId
import codes.reactor.kernel.plugin.model.lifecycle.PluginState

internal class PluginStartupCancelledException(
    val pluginId: PluginId,
    val phase: PluginState,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
