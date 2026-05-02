package codes.reactor.microkernel.plugin.scope

import codes.reactor.kernel.event.EventBus
import codes.reactor.kernel.plugin.scope.PluginDependencyProvider
import codes.reactor.kernel.plugin.scope.PluginScope
import codes.reactor.microkernel.Microkernel
import codes.reactor.microkernel.plugin.classloading.PluginClassLoader
import codes.reactor.microkernel.plugin.scope.extension.KernelPluginEventBusScope
import java.util.concurrent.ConcurrentHashMap

internal class KernelPluginScope : PluginScope {

    private val providers = ConcurrentHashMap<Class<*>, PluginDependencyProvider<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(type: Class<T>): T? {
        var provider = providers[type]

        if (provider != null) {
            return provider as T
        }

        provider = when {
            EventBus::class.java.isAssignableFrom(type) -> KernelPluginEventBusScope(Microkernel.instance.rootBus)
            else -> null
        }

        if (provider != null) {
            providers[type] = provider
            return provider.provide() as T
        }
        return null
    }

    override fun <T : PluginDependencyProvider<T>> manage(resource: T): T {
        providers[resource.javaClass] = resource
        return resource
    }

    override fun unmanage(type: Class<Any>): PluginDependencyProvider<*>? {
        return providers.remove(type)
    }

    override fun close() {
        providers.values.forEach {
            try {
                it.close()
            } catch (e: Throwable) {
                val pluginID = (Thread.currentThread().contextClassLoader as PluginClassLoader).id
                Microkernel.instance.rootLogger.error("Error on close resource in the plugin $pluginID", e)
            }
        }
        providers.clear()
    }
}
