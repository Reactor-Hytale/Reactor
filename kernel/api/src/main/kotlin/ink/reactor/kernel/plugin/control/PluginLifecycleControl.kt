package codes.reactor.kernel.plugin.control

import codes.reactor.kernel.plugin.model.PluginId
import codes.reactor.kernel.plugin.exception.PluginNotFoundException
import codes.reactor.kernel.plugin.exception.PluginOperationInProgressException
import codes.reactor.kernel.plugin.exception.PluginTransitionNotAllowedException

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
