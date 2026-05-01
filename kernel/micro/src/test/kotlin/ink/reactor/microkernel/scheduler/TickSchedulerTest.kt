package ink.reactor.microkernel.scheduler

import ink.reactor.kernel.scheduler.tick.Ticks
import ink.reactor.microkernel.scheduler.tick.TickScheduler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

class TickSchedulerTest {

    private fun createDefaultScheduler() = TickScheduler(Ticks.ZERO, 50.milliseconds)

    @Test
    fun `test runNow executes on first tick`() {
        val tickScheduler = createDefaultScheduler()
        val executed = AtomicInteger(0)

        tickScheduler.runNow { executed.incrementAndGet() }
        tickScheduler.tick()

        assertEquals(1, executed.get())
    }

    @Test
    fun `test later tasks timing`() {
        val tickScheduler = createDefaultScheduler()
        val task1 = AtomicBoolean(false)
        val task2 = AtomicBoolean(false)
        val task3 = AtomicBoolean(false)

        tickScheduler.runAfterDelay({ task1.set(true) }, Ticks.ZERO)
        tickScheduler.runAfterDelay({ task2.set(true) }, Ticks(2))
        tickScheduler.runAtTick({ task3.set(true) }, Ticks(3))

        // Tick 1
        tickScheduler.tick()
        assertTrue(task1.get(), "Task 1 should execute at Tick 1")
        assertFalse(task2.get())

        // Tick 2
        tickScheduler.tick()
        assertFalse(task2.get())

        // Tick 3
        tickScheduler.tick()
        assertTrue(task2.get(), "Task 2 (delay 2) should execute at Tick 3")
        assertTrue(task3.get(), "Task 3 (runAt 3) should execute at Tick 3")
    }

    @Test
    fun `test scheduled repeating tasks`() {
        val tickScheduler = createDefaultScheduler()
        val task1 = AtomicInteger(0)
        val task2 = AtomicInteger(0)

        // Delay 2 -> Tick 3, Delay 3 -> Tick 7 (3 + 3 + 1)
        val taskId = tickScheduler.scheduleWithDelayBetween(
            { task1.incrementAndGet() },
            delayFirstExecute = Ticks(2),
            delayBetweenExecute = Ticks(3)
        )

        // Every 3 -> Tick 1, 4, 7...
        val task2Id = tickScheduler.scheduleEvery(
            { task2.incrementAndGet() },
            tickToStart = Ticks(1),
            executeInTheTick = Ticks(3)
        )

        // Tick 1
        tickScheduler.tick()
        assertEquals(0, task1.get())
        assertEquals(1, task2.get(), "Task 2 starts at Tick 1")

        // Tick 2
        tickScheduler.tick()
        assertEquals(0, task1.get())
        assertEquals(1, task2.get())

        // Tick 3
        tickScheduler.tick()
        assertEquals(1, task1.get(), "Task 1 starts after delay 2 (Tick 3)")
        assertEquals(1, task2.get())

        // Tick 4
        tickScheduler.tick()
        assertEquals(1, task1.get())
        assertEquals(2, task2.get(), "Task 2 repeats every 3 ticks (1 + 3 = 4)")

        // Skip to Tick 7
        tickScheduler.tick() // Tick 5
        tickScheduler.tick() // Tick 6
        tickScheduler.tick() // Tick 7

        assertEquals(2, task1.get(), "Task 1 second execution at Tick 7 (3 + 3 + 1)")
        assertEquals(3, task2.get(), "Task 2 third execution at Tick 7 (4 + 3)")

        // Cancel
        tickScheduler.cancelTask(taskId.id)
        task2Id.cancel()

        assertTrue(task2Id.canceled, "Task 2 cancelled")

        repeat(5) { tickScheduler.tick() }
        assertEquals(2, task1.get())
        assertEquals(3, task2.get())
    }

    @Test
    fun `test runAfterDelay with Duration executes at correct tick`() {
        val scheduler = createDefaultScheduler()
        val executions = AtomicInteger(0)

        // 150ms / 50ms = 3 Ticks of delay
        scheduler.runAfterDelay({ executions.incrementAndGet() }, 150.milliseconds)

        scheduler.tick() // Tick 1
        scheduler.tick() // Tick 2
        scheduler.tick() // Tick 3
        assertEquals(0, executions.get(), "Should not execute yet, it's in delay")

        scheduler.tick() // Tick 4
        assertEquals(1, executions.get(), "Must execute at Tick 4 after 150ms delay")
    }

    @Test
    fun `test runAt with Duration executes at exact absolute time`() {
        val scheduler = createDefaultScheduler()
        val executions = AtomicInteger(0)

        // 100ms / 50ms = Need be executed in the Tick 2
        scheduler.runAt({ executions.incrementAndGet() }, 100.milliseconds)

        scheduler.tick() // Tick 1: 50ms
        assertEquals(0, executions.get())

        scheduler.tick() // Tick 2: 100ms
        assertEquals(1, executions.get(), "Must execute exactly at the equivalent of 100ms (Tick 2)")
    }

    @Test
    fun `test scheduleEvery with Duration intervals`() {
        val scheduler = createDefaultScheduler()
        val executions = AtomicInteger(0)

        val task = scheduler.scheduleEvery(
            task = { executions.incrementAndGet() },
            startTime = 50.milliseconds,
            interval = 100.milliseconds
        )

        scheduler.tick() // Tick 1 (50ms) -> First execution
        assertEquals(1, executions.get())

        scheduler.tick() // Tick 2 (100ms) -> Wait
        assertEquals(1, executions.get())

        scheduler.tick() // Tick 3 (150ms) -> Second execution (50 + 100)
        assertEquals(2, executions.get())

        scheduler.tick() // Tick 4 (200ms) -> Wait
        scheduler.tick() // Tick 5 (250ms) -> Third execution
        assertEquals(3, executions.get())

        task.cancel()
    }

    @Test
    fun `test throws exception if Duration is smaller than minPrecisionUnit`() {
        val scheduler = createDefaultScheduler()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            scheduler.runAfterDelay({}, 20.milliseconds)
        }

        assertTrue(exception.message!!.contains("smaller than precision"))
    }
}
