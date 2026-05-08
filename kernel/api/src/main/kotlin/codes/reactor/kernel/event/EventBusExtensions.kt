package codes.reactor.kernel.event

import codes.reactor.kernel.event.handler.ListenerPhase

inline fun <reified T : Any> EventBus.subscribe(
    phase: ListenerPhase = ListenerPhase.DEFAULT,
    priority: Int = 0,
    ignoreCancelled: Boolean = false,
    noinline block: (T) -> Unit
): Subscription {
    return this.subscribe(T::class.java, ignoreCancelled, priority, phase, block)
}
