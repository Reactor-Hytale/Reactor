package codes.reactor.kernel.plugin.model.failure

import codes.reactor.kernel.plugin.model.lifecycle.PluginState
import java.time.Instant

data class PluginFailure(
    val state: PluginState,
    val exception: PluginExceptionDetails? = null,
    val timestamp: Instant = Instant.now()
)
