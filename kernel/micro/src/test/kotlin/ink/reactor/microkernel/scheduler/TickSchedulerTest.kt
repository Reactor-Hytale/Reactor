package ink.reactor.microkernel.scheduler

import ink.reactor.kernel.scheduler.Ticks
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration

class TickSchedulerTest {

    fun createDefaultScheduler(): TickScheduler {
        return TickScheduler(Ticks.ZERO, Duration.ZERO)
    }

    @Test
    fun testNowTasks() {
        val tickScheduler = createDefaultScheduler()
        val executed = AtomicInteger()

        tickScheduler.runNow { executed.incrementAndGet() }
        tickScheduler.runNow { executed.incrementAndGet() }
        tickScheduler.runNow { executed.incrementAndGet() }

        tickScheduler.tick()

        Assertions.assertEquals(3, executed.get())
    }

    @Test
    fun testLaterTasks() {
        val tickScheduler = createDefaultScheduler()
        val ticksElapsed = AtomicInteger()

        val task1 = AtomicBoolean()
        val task2 = AtomicBoolean()
        val task3 = AtomicBoolean()

        tickScheduler.runAfterDelay({ task1.set(true) }, Ticks.ZERO)
        tickScheduler.runAfterDelay({ task2.set(true) }, Ticks(2))
        tickScheduler.runAtTick({ task3.set(true) }, Ticks(3))

        for (i in 1..4) {
            tickScheduler.tick()
            ticksElapsed.set(i)

            if (ticksElapsed.get() < 1) {
                Assertions.assertFalse(task1.get(), "Task1 executed too early")
            }
            if (ticksElapsed.get() < 3) { // 2 delay + 1
                Assertions.assertFalse(task2.get(), "Task2 executed too early at tick ${ticksElapsed.get()}")
            }
            if (ticksElapsed.get() < 3) {
                Assertions.assertFalse(task3.get(), "Task3 executed too early at tick ${ticksElapsed.get()}")
            }
        }

        Assertions.assertTrue(task1.get(), "Task1 should have executed")
        Assertions.assertTrue(task2.get(), "Task2 should have executed")
        Assertions.assertTrue(task3.get(), "Task3 should have executed")
    }

    @Test
    fun testScheduleAfterTasks() {
        val tickScheduler = createDefaultScheduler()
        val task1 = AtomicInteger()
        val task2 = AtomicInteger()

        val firstTaskId = tickScheduler.scheduleWithDelayBetween(
            { task1.incrementAndGet() },
            Ticks(2),
            Ticks(3)
        )

        val secondTaskId = tickScheduler.scheduleEvery(
            { task2.incrementAndGet() },
            Ticks(1),
            Ticks(3)
        )

        // Tick 1 (before starting)
        tickScheduler.tick()
        Assertions.assertEquals(0, task1.get(), "Task1: Should not execute on first tick")
        Assertions.assertEquals(1, task2.get(), "Task2: Should not execute yet")

        // Tick 2
        tickScheduler.tick()
        Assertions.assertEquals(0, task1.get(), "Task1: Should not execute yet")

        // Tick 3 (first execution)
        tickScheduler.tick()
        Assertions.assertEquals(1, task1.get(), "Task1: First execution on tick 2")

        // Tick 4
        tickScheduler.tick()
        Assertions.assertEquals(1, task1.get(), "Task1: Should not repeat yet")
        Assertions.assertEquals(2, task2.get(), "Task2: Should not repeat yet")

        // Tick 5
        tickScheduler.tick()
        Assertions.assertEquals(1, task1.get(), "Task1: Should not repeat yet")

        // Tick 6
        tickScheduler.tick()
        Assertions.assertEquals(1, task1.get(), "Task1: Should not repeat yet")

        // Tick 7 (second execution - 3 ticks after first execution)
        tickScheduler.tick()
        Assertions.assertEquals(2, task1.get(), "Task1: Second execution on tick 7")
        Assertions.assertEquals(3, task2.get(), "Task2: Three execution on tick 7")

        // Tick 8
        tickScheduler.tick()
        Assertions.assertEquals(2, task1.get(), "Task1: Should not repeat yet")

        // Cancel the task
        tickScheduler.cancelScheduleTask(firstTaskId)
        tickScheduler.cancelScheduleTask(secondTaskId)

        // Tick 9 (should not execute after cancellation)
        for (i in 0..9) {
            tickScheduler.tick()
        }
        Assertions.assertEquals(2, task1.get(), "Task1: Should not execute after cancellation")
        Assertions.assertEquals(3, task2.get(), "Task2: Should not execute after cancellation")
    }
}
