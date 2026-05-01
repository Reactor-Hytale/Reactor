package ink.reactor.kernel.event

import ink.reactor.kernel.event.handler.EventHandler
import ink.reactor.kernel.event.handler.ListenerPhase

/**
 * Manages the registration and dispatching of events.
 *
 * <p>The EventBus allows for decoupled communication between components by
 * providing a centralized system for posting and listening to events.</p>
 */
interface EventBus {
    /**
     * Registers all methods in the given object that are annotated with {@link Listener}.
     *
     * @param listener The object containing listener methods.
     * @return A subscription representing all listeners registered for this object.
     */
    fun subscribe(listener: Any): Subscription

    /**
     * Registers a listener with a custom executor for granular control.
     *
     * @param eventClass The class of the event to listen for.
     * @param phase The phase in which this listener will execute.
     * @return A subscription representing this registration.
     */
    fun <T : Any> subscribe(
        eventClass: Class<T>,
        ignoreCancelled: Boolean = false,
        priority: Int = 0,
        phase: ListenerPhase = ListenerPhase.DEFAULT,
        block: (T) -> Unit
    ): Subscription

    /**
     * Registers a listener with a custom executor for granular control.
     *
     * @param handler
     * @return A subscription representing this registration.
     */
    fun subscribe(handler: EventHandler): Subscription

    /**
     * Unregisters all listeners associated with the given subscription.
     *
     * @param subscription The subscription to revoke.
     */
    fun unsubscribe(subscription: Subscription)

    /**
     * Dispatches an event to all registered listeners.
     *
     * @param event The event object to post.
     */
    fun post(event: Any)

    /**
     * Removes all registered listeners from this bus.
     */
    fun clear()
}
