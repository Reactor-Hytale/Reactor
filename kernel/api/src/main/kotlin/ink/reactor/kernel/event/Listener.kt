package ink.reactor.kernel.event

import ink.reactor.kernel.event.handler.ListenerPhase
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationRetention.RUNTIME

@Target(FUNCTION)
@Retention(RUNTIME)
annotation class Listener(
    /**
     * The lower the phase, the sooner it will be executed
     */
    val phase: ListenerPhase = ListenerPhase.DEFAULT,

    /**
     * Highest priority = Runs before
     * Low priority = Runs after
     * @return execution priority
     */
    val priority: Int = 0,

    /**
     * @return if you can run the listener, ignoring whether the event is canceled or not
     */
    val ignoreCancelled: Boolean = false,
)
