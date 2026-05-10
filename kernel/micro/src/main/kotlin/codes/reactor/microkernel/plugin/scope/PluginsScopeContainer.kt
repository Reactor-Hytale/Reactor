package codes.reactor.microkernel.plugin.scope

import codes.reactor.kernel.plugin.scope.PluginScope
import codes.reactor.microkernel.Microkernel
import java.util.concurrent.ConcurrentHashMap

object PluginsScopeContainer {
    private val scopes = ConcurrentHashMap<ClassLoader, PluginScope>()

    fun getOrCreateScope(classLoader: ClassLoader, supplyFunction: () -> PluginScope): PluginScope {
        return scopes.computeIfAbsent(classLoader) { supplyFunction() }
    }

    fun removeScope(classLoader: ClassLoader): PluginScope? {
        return scopes.remove(classLoader)
    }
}
