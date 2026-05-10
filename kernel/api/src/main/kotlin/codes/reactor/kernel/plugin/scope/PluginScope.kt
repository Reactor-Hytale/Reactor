package codes.reactor.kernel.plugin.scope

import codes.reactor.kernel.plugin.scope.provider.DependencyProvider

interface PluginScope {

    operator fun <T : Any> get(type: Class<T>): T?
    fun <T : Any> getProvider(type: Class<T>): DependencyProvider<T>?

    fun <T: Any> put(provider: DependencyProvider<T>)

    fun remove(type: Class<*>): DependencyProvider<*>?
}
