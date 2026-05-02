package ink.reactor.microkernel.plugin.scope

import ink.reactor.kernel.plugin.scope.PluginScope
import ink.reactor.kernel.plugin.scope.PluginScopeFactory
import ink.reactor.microkernel.plugin.classloading.PluginClassLoader

class KernelPluginScopeFactory : PluginScopeFactory {

    override fun acquire(): PluginScope {
        val classLoader = Thread.currentThread().contextClassLoader
        if (classLoader !is PluginClassLoader) {
            throw IllegalStateException("Attempted to acquire a plugin scope from a non-plugin thread/context")
        }
        return PluginsScopeContainer.getOrCreateScope(classLoader) { create() }
    }

    override fun create(): PluginScope {
        return KernelPluginScope()
    }
}
