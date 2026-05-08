package codes.reactor.kernel.plugin.exception

import codes.reactor.kernel.plugin.model.lifecycle.PluginState

class PluginTransitionNotAllowedException(
    currentState: PluginState,
    targetState: PluginState,
    errorMessage: String? = null,
): PluginLifecycleException("Plugin transition from $currentState to $targetState is not allowed. ${errorMessage ?: ""}")
