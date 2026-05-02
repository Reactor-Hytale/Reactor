package codes.reactor.kernel.plugin.model

import codes.reactor.kernel.plugin.model.failure.PluginFailure
import codes.reactor.kernel.plugin.model.lifecycle.PluginState

data class PluginSnapshot(
    val state: PluginState,
    val failure: PluginFailure? = null,
    val metadata: PluginMetadata
)
