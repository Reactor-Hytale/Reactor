package ink.reactor.kernel.plugin.scope

interface PluginScope : AutoCloseable {

    operator fun <T : Any> get(type: Class<T>): T?
    fun <T : PluginDependencyProvider<T>> manage(resource: T): T
    fun unmanage(type: Class<Any>): PluginDependencyProvider<*>?
}
