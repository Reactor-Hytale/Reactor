package codes.reactor.microkernel.plugin.scope

import codes.reactor.kernel.plugin.scope.provider.DependencyProvider
import codes.reactor.kernel.plugin.scope.provider.CloseableDependencyProvider
import codes.reactor.kernel.plugin.scope.PluginScope
import codes.reactor.kernel.plugin.scope.provider.ProtectedDependencyProvider
import codes.reactor.microkernel.Microkernel
import codes.reactor.microkernel.plugin.classloading.PluginClassLoader

internal class KernelPluginScope : PluginScope, AutoCloseable {

    private val providers = HashMap<Class<*>, DependencyProvider<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(type: Class<T>): T? {
        synchronized(providers) {
            return providers[type]?.provide() as? T
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getProvider(type: Class<T>): DependencyProvider<T> {
        synchronized(providers) {
            return providers[type] as DependencyProvider<T>
        }
    }

    override fun <T : Any> put(provider: DependencyProvider<T>) {
        synchronized(providers) {
            val type = provider.type
            val oldProvider = providers[type]

            if (oldProvider is ProtectedDependencyProvider) {
                throw IllegalStateException("Cannot replace protected provider of type $type")
            }

            closeProvider(oldProvider)
            providers[type] = provider
        }
    }

    override fun remove(type: Class<*>): DependencyProvider<*>? {
        synchronized(providers) {
            val provider = providers[type]

            if (provider is ProtectedDependencyProvider) {
                throw IllegalStateException(
                    "Cannot remove protected provider of type $type"
                )
            }

            return providers.remove(type)
        }
    }

    override fun close() {
        synchronized(providers) {
            providers.values.forEach(::closeProvider)
            providers.clear()
        }
    }

    private fun closeProvider(provider: DependencyProvider<*>?) {
        if (provider !is CloseableDependencyProvider<*>) {
            return
        }

        runCatching {
            provider.close()
        }.onFailure { throwable ->
            val pluginId = (Thread.currentThread().contextClassLoader as PluginClassLoader).id

            Microkernel.instance.rootLogger.error(
                "Error while closing provider in plugin $pluginId",
                throwable
            )
        }
    }
}
