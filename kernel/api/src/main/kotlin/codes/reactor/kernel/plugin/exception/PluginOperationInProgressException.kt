package codes.reactor.kernel.plugin.exception

import codes.reactor.kernel.plugin.model.PluginId
import codes.reactor.kernel.plugin.model.lifecycle.PluginState

class PluginOperationInProgressException(
    pluginId: PluginId,
    currentState: PluginState,
    targetState: PluginState,
): PluginLifecycleException("Plugin $pluginId is currently in state $currentState and cannot transition to $targetState because another operation is in progress.")
