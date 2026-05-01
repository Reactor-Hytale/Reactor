package ink.reactor.microkernel.plugin.scope

import ink.reactor.kernel.plugin.model.PluginId
import ink.reactor.kernel.plugin.scope.PluginDependencyProvider
import ink.reactor.kernel.plugin.scope.PluginScope
import java.util.concurrent.ConcurrentHashMap

class KernelPluginScope(override val pluginId: PluginId) : PluginScope {

    private val providers = ConcurrentHashMap<Class<*>, PluginDependencyProvider<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(type: Class<T>): T? {
        return providers[type]?.provide() as T?
    }

    override fun <T : PluginDependencyProvider<T>> manage(resource: T): T {
        providers[resource.javaClass] = resource
        return resource
    }

    override fun unmanage(type: Class<Any>): PluginDependencyProvider<*>? {
        return providers.remove(type)
    }

    override fun close() {
        providers.values.forEach { it.close() }
        providers.clear()
    }
}
