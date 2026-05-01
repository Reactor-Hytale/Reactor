package ink.reactor.kernel.plugin.scope

interface PluginDependencyProvider<T>: AutoCloseable {
    fun provide(): T
}
