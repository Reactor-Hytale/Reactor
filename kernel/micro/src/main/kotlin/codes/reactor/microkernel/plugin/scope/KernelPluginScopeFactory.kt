package codes.reactor.microkernel.plugin.scope

import codes.reactor.kernel.plugin.scope.PluginScope
import codes.reactor.kernel.plugin.scope.PluginScopeFactory
import codes.reactor.kernel.plugin.scope.provider.DependencyProvider
import codes.reactor.microkernel.plugin.classloading.PluginClassLoader
import codes.reactor.microkernel.plugin.scope.provider.ScopedEventBusProvider

object KernelPluginScopeFactory : PluginScopeFactory {

    private val defaultDependencyProviders: MutableList<DependencyProvider<*>> = mutableListOf(
        ScopedEventBusProvider.fromLazy()
    )

    fun registerDefaultProvider(provider: DependencyProvider<*>) {
        defaultDependencyProviders.add(provider)
    }

    override fun acquire(): PluginScope {
        val classLoader = Thread.currentThread().contextClassLoader
        if (classLoader !is PluginClassLoader) {
            throw IllegalStateException("Attempted to acquire a plugin scope from a non-plugin thread/context")
        }
        return PluginsScopeContainer.getOrCreateScope(classLoader) { create() }
    }

    override fun create(): PluginScope {
        val kernelPluginScope = KernelPluginScope()
        defaultDependencyProviders.forEach { provider -> kernelPluginScope.put(provider) }
        return kernelPluginScope
    }
}
