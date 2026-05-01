package ink.reactor.kernel.event.dispatch

interface EventExecutor {
    fun execute(event: Any)
}
