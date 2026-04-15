package ink.reactor.kernel.plugin.model

import ink.reactor.kernel.plugin.model.failure.PluginFailure
import ink.reactor.kernel.plugin.model.lifecycle.PluginState

data class PluginSnapshot(
    val state: PluginState,
    val failure: PluginFailure? = null,
    val metadata: PluginMetadata
)
