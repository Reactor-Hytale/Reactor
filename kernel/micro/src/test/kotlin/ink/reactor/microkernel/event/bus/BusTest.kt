package ink.reactor.microkernel.event.bus

import ink.reactor.kernel.event.Cancellable
import ink.reactor.kernel.event.subscribe
import ink.reactor.microkernel.logger.PrintlnLogger
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

import ink.reactor.kernel.event.Listener
import ink.reactor.kernel.event.handler.ListenerPhase
import kotlin.test.assertFalse

class BusTest {

    @Test
    fun `should receive event when subscribed using lambda`() {
        val eventBus = DefaultEventBus(PrintlnLogger())
        var receivedUsername = ""

        eventBus.subscribe<String> { name -> receivedUsername = name }

        eventBus.publish("ManoloGaymer")

        assertEquals("ManoloGaymer", receivedUsername, "The listener should receive the exact event posted")
    }

    @Test
    fun `should respect listener priority and phases`() {
        val eventBus = DefaultEventBus(PrintlnLogger())
        val executionOrder = mutableListOf<String>()

        eventBus.subscribe<ChatEvent>(ListenerPhase.MONITOR) { executionOrder.add("MONITOR") }
        eventBus.subscribe<ChatEvent>(ListenerPhase.FINAL) { executionOrder.add("FINAL") }
        eventBus.subscribe<ChatEvent>(ListenerPhase.LATE) { executionOrder.add("LATE") }
        eventBus.subscribe<ChatEvent>(ListenerPhase.DEFAULT, priority = 100) { executionOrder.add("DEFAULT_HIGH_PRIORITY") }
        eventBus.subscribe<ChatEvent>(ListenerPhase.DEFAULT, priority = -10) { executionOrder.add("DEFAULT_LOW_PRIORITY") }
        eventBus.subscribe<ChatEvent>(ListenerPhase.EARLY) { executionOrder.add("EARLY") }
        eventBus.subscribe<ChatEvent>(ListenerPhase.INITIAL) { executionOrder.add("INITIAL") }

        eventBus.publish(ChatEvent("Hello Server!"))

        val expectedOrder = listOf("INITIAL", "EARLY", "DEFAULT_HIGH_PRIORITY", "DEFAULT_LOW_PRIORITY", "LATE", "FINAL", "MONITOR")
        assertEquals(expectedOrder, executionOrder, "Events must be executed following strict Phase and Priority order")
    }

    @Test
    fun `should stop receiving events after unregistering subscription`() {
        val eventBus = DefaultEventBus(PrintlnLogger())
        var eventCount = 0

        assertEquals(0, eventBus.size())
        val subscription = eventBus.subscribe<UserJoinEvent> { eventCount++ }
        assertEquals(1, eventBus.size())

        eventBus.publish(UserJoinEvent("Player1"))
        subscription.unsubscribe()
        assertEquals(0, eventBus.size())
        eventBus.publish(UserJoinEvent("Player2"))

        assertEquals(1, eventCount, "Listener must not be triggered after unregister is called")
    }

    @Test
    fun `should process annotated listeners correctly`() {
        val eventBus = DefaultEventBus(PrintlnLogger())
        val exampleJoinListener = ExampleJoinListener()

        eventBus.subscribe(exampleJoinListener)

        eventBus.publish(UserJoinEvent("Spammer"))

        assertTrue(exampleJoinListener.wasChecked, "Annotated methods must be registered and executed")
    }

    @Test
    fun `should skip normal listeners if event is cancelled`() {
        val eventBus = DefaultEventBus(PrintlnLogger())
        var normalListenerRan = false
        var ignoreCancelledListenerRan = false

        eventBus.subscribe<DamageEvent>(ListenerPhase.INITIAL) {
            event -> event.cancelled = true
        }
        eventBus.subscribe<DamageEvent>(ListenerPhase.DEFAULT) {
            normalListenerRan = true
        }
        eventBus.subscribe<DamageEvent>(ListenerPhase.MONITOR, ignoreCancelled = true) {
            ignoreCancelledListenerRan = true
        }

        eventBus.publish(DamageEvent(10.0))
        assertFalse(normalListenerRan, "Normal listeners should skip cancelled events")
        assertTrue(ignoreCancelledListenerRan, "Listeners with ignoreCancelled=true must run")
    }

    // Example events
    data class UserJoinEvent(val username: String)
    data class ChatEvent(val message: String)
    data class DamageEvent(
        val amount: Double,
        override var cancelled: Boolean = false) : Cancellable

    class ExampleJoinListener {
        var wasChecked = false

        @Listener(ListenerPhase.EARLY)
        fun onUserJoin(event: UserJoinEvent) {
            wasChecked = true
        }
    }
}
