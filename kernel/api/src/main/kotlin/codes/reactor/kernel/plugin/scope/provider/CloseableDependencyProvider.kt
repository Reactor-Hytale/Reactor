package codes.reactor.kernel.plugin.scope.provider

interface CloseableDependencyProvider<T : Any> : DependencyProvider<T> {
    fun close()
}
