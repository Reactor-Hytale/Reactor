package ink.reactor.kernel.plugin.scope

import ink.reactor.kernel.plugin.model.PluginId

interface PluginScope : AutoCloseable {

    val pluginId: PluginId

    operator fun <T : Any> get(type: Class<T>): T?
    fun <T : PluginDependencyProvider<T>> manage(resource: T): T
    fun unmanage(type: Class<Any>): PluginDependencyProvider<*>?
}
