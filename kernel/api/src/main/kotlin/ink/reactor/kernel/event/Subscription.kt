package codes.reactor.kernel.event

import codes.reactor.kernel.event.handler.EventHandler

/**
 * Represents a handle to a group of registered listeners.
 * Provides a way to manage the lifecycle of the registration.
 */
interface Subscription {
    /**
     * Removes all listeners associated with this subscription from the bus.
     */
    fun unsubscribe()

    val handlers: Collection<EventHandler>
}
