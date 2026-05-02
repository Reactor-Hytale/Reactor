package codes.reactor.kernel.event.handler

import codes.reactor.kernel.event.dispatch.EventExecutor

/**
 * Data container representing a registered event listener.
 *
 * @property eventClass The type of event being listened to.
 * @property executor The strategy used to execute the listener logic.
 * @property phase The chronological phase for execution.
 * @property priority The priority within the same phase (higher runs first).
 */
data class EventHandler(
    val eventClass: Class<*>,
    val executor: EventExecutor,
    val phase: ListenerPhase,
    val priority: Int,
)
