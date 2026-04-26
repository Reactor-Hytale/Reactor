package ink.reactor.kernel.plugin.exception

import ink.reactor.kernel.plugin.model.PluginId
import ink.reactor.kernel.plugin.model.lifecycle.PluginState

class PluginLifecycleExecutionException(
    pluginId: PluginId,
    phase: PluginState,
    cause: Throwable
) : PluginLifecycleException(
    "Plugin $pluginId failed during $phase.",
    cause
)
