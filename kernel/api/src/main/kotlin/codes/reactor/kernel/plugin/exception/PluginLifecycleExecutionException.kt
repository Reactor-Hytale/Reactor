package codes.reactor.kernel.plugin.exception

import codes.reactor.kernel.plugin.model.PluginId
import codes.reactor.kernel.plugin.model.lifecycle.PluginState

class PluginLifecycleExecutionException(
    pluginId: PluginId,
    phase: PluginState,
    cause: Throwable
) : PluginLifecycleException(
    "Plugin $pluginId failed during $phase.",
    cause
)
