package codes.reactor.microkernel.plugin.scope.extension

import codes.reactor.kernel.event.EventBus
import codes.reactor.kernel.event.Subscription
import codes.reactor.kernel.event.handler.EventHandler
import codes.reactor.kernel.event.handler.ListenerPhase
import codes.reactor.kernel.plugin.scope.PluginDependencyProvider
import java.util.Collections

class KernelPluginEventBusScope(
    private val delegate: EventBus,
    val subscriptions: MutableList<Subscription> = Collections.synchronizedList(mutableListOf()),
) : EventBus, PluginDependencyProvider<EventBus> {

    private fun addSubscription(subscription: Subscription) {
        if (!subscription.handlers.isEmpty()) {
            this.subscriptions.add(subscription)
        }
    }

    override fun subscribe(listener: Any): Subscription {
        val subscription = delegate.subscribe(listener)
        addSubscription(subscription)
        return subscription
    }

    override fun <T : Any> subscribe(
        eventClass: Class<T>,
        ignoreCancelled: Boolean,
        priority: Int,
        phase: ListenerPhase,
        block: (T) -> Unit
    ): Subscription {
        val subscription = delegate.subscribe(eventClass, ignoreCancelled, priority, phase, block)
        addSubscription(subscription)
        return subscription
    }

    override fun subscribe(handler: EventHandler): Subscription {
        val subscription = delegate.subscribe(handler)
        addSubscription(subscription)
        return subscription
    }

    override fun unsubscribe(subscription: Subscription) {
        subscription.unsubscribe()
        subscriptions.remove(subscription)
    }

    override fun publish(event: Any) {
        delegate.publish(event)
    }

    override fun clear() {
        for (subscription in subscriptions) {
            subscription.unsubscribe()
        }
    }

    override fun provide(): EventBus {
        return this
    }

    override fun close() {
        this.clear()
    }
}
