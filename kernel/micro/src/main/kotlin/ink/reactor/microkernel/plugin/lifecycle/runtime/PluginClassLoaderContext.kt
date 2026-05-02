package codes.reactor.microkernel.plugin.lifecycle.runtime

internal inline fun <T> withPluginClassLoader(classLoader: ClassLoader, block: () -> T): T {
    val thread = Thread.currentThread()
    val previous = thread.contextClassLoader
    thread.contextClassLoader = classLoader
    return try {
        block()
    } finally {
        thread.contextClassLoader = previous
    }
}
