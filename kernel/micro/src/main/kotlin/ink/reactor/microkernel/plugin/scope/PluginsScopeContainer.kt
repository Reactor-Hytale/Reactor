package codes.reactor.microkernel.plugin.scope

import codes.reactor.kernel.plugin.scope.PluginScope
import codes.reactor.microkernel.Microkernel
import java.util.concurrent.ConcurrentHashMap

object PluginsScopeContainer {
    private val scopes = ConcurrentHashMap<ClassLoader, PluginScope>()

    fun getOrCreateScope(classLoader: ClassLoader, supplyFunction: () -> PluginScope): PluginScope {
        return scopes.computeIfAbsent(classLoader) { supplyFunction() }
    }

    fun closeScope(classLoader: ClassLoader) {
        val scope = scopes.remove(classLoader) ?: return
        runCatching { scope.close() }.onFailure {
            Microkernel.instance.rootLogger.error("Failed to close root scope for classloader: $classLoader", it)
        }
    }
}
