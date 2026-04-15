package ink.reactor.kernel.plugin.control

import ink.reactor.kernel.plugin.model.PluginId
import ink.reactor.kernel.plugin.exception.PluginNotFoundException
import ink.reactor.kernel.plugin.exception.PluginOperationInProgressException
import ink.reactor.kernel.plugin.exception.PluginTransitionNotAllowedException

interface PluginLifecycleControl {
    @Throws(
        PluginNotFoundException::class,
        PluginTransitionNotAllowedException::class,
        PluginOperationInProgressException::class
    )
    fun load(id: PluginId)

    @Throws(
        PluginNotFoundException::class,
        PluginTransitionNotAllowedException::class,
        PluginOperationInProgressException::class
    )
    fun enable(id: PluginId)

    @Throws(
        PluginNotFoundException::class,
        PluginTransitionNotAllowedException::class,
        PluginOperationInProgressException::class
    )
    fun disable(id: PluginId)
}
