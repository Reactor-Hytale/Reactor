package codes.reactor.kernel.plugin.scope.provider

interface DependencyProvider<T : Any> {
    val type: Class<T>
    fun provide(): T
}
