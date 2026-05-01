package ink.reactor.microkernel.scheduler.tick

import ink.reactor.kernel.scheduler.Task
import ink.reactor.kernel.scheduler.exception.SchedulerClosedException
import ink.reactor.kernel.scheduler.tick.TickDrivenScheduler
import ink.reactor.kernel.scheduler.tick.Ticks
import ink.reactor.kernel.scheduler.tick.toTicks
import ink.reactor.microkernel.scheduler.DefaultTask
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.max
import kotlin.time.Duration

class TickScheduler(
    currentTick: Ticks = Ticks.ZERO,
    override val minPrecisionUnit: Duration
) : TickDrivenScheduler {

    init {
        require(minPrecisionUnit > Duration.ZERO) {
            "minPrecisionUnit must be greater than zero"
        }
    }

    private val tickMutex = ReentrantLock()
    private val stateMutex = ReentrantLock()

    private val tickCounter = AtomicLong(currentTick.duration)

    @Volatile
    private var acceptingTasks = true

    @Volatile
    private var stopped = false

    private val nowTasks = NowTasks()
    private val laterTasks = LaterTasks()
    private val scheduleTasks = ScheduleTasks()

    private val executionBuffer = TaskBatch()

    override val currentTick: Ticks
        get() = Ticks(tickCounter.get())

    override fun tick() {
        runTicks(1L)
    }

    override fun tick(amount: Ticks) {
        runTicks(amount.duration)
    }

    private fun runTicks(amount: Long) {
        if (amount <= 0L) {
            return
        }

        if (tickMutex.isHeldByCurrentThread) {
            throw IllegalStateException(
                "TickScheduler.tick() cannot be called from inside a scheduled task"
            )
        }

        tickMutex.withLock {
            var remaining = amount

            while (remaining > 0L) {
                executionBuffer.clear()

                val shouldExecute = stateMutex.withLock {
                    if (stopped) {
                        false
                    } else {
                        tickCounter.incrementAndGet()

                        nowTasks.drainTo(executionBuffer)
                        laterTasks.tickAndDrainTo(executionBuffer)
                        scheduleTasks.tickAndDrainTo(executionBuffer)

                        true
                    }
                }

                if (!shouldExecute) {
                    break
                }

                executionBuffer.runAndClear()
                remaining--
            }
        }
    }

    override fun runNow(task: () -> Unit) {
        stateMutex.withLock {
            ensureAcceptingTasks()
            nowTasks.addTask(task)
        }
    }

    override fun runAt(task: () -> Unit, timeToExecute: Duration) {
        runAtTick(task, durationToTicks(timeToExecute))
    }

    override fun runAfterDelay(task: () -> Unit, delay: Duration) {
        runAfterDelay(task, durationToTicks(delay))
    }

    override fun scheduleEvery(
        task: () -> Unit,
        startTime: Duration,
        interval: Duration
    ): Task {
        return scheduleEvery(
            task,
            tickToStart = durationToTicks(startTime),
            executeInTheTick = durationToTicks(interval)
        )
    }

    override fun scheduleWithDelay(
        task: () -> Unit,
        initialDelay: Duration,
        delayBetweenExecute: Duration
    ): Task {
        return scheduleWithDelayBetween(
            task,
            delayFirstExecute = durationToTicks(initialDelay),
            delayBetweenExecute = durationToTicks(delayBetweenExecute)
        )
    }

    override fun runAtTick(task: () -> Unit, tickToExecute: Ticks) {

        stateMutex.withLock {
            ensureAcceptingTasks()

            val relativeTicks = tickToExecute.duration - tickCounter.get() - 1L

            if (relativeTicks < 0L) {
                nowTasks.addTask(task)
            } else {
                laterTasks.addTask(task, relativeTicks)
            }
        }
    }

    override fun runAfterDelay(task: () -> Unit, delay: Ticks) {
        stateMutex.withLock {
            ensureAcceptingTasks()

            if (delay.duration <= 0L) {
                nowTasks.addTask(task)
            } else {
                laterTasks.addTask(task, delay.duration)
            }
        }
    }

    override fun scheduleEvery(
        task: () -> Unit,
        tickToStart: Ticks,
        executeInTheTick: Ticks
    ): Task {
        val id = stateMutex.withLock {
            ensureAcceptingTasks()

            val initialDelay = max(
                0L,
                tickToStart.duration - tickCounter.get() - 1L
            )

            val intervalDelay = max(
                0L,
                executeInTheTick.duration - 1L
            )

            scheduleTasks.addTask(
                task,
                initialDelay,
                intervalDelay
            )
        }

        return DefaultTask(id, false, this)
    }

    override fun scheduleWithDelayBetween(
        task: () -> Unit,
        delayFirstExecute: Ticks,
        delayBetweenExecute: Ticks
    ): Task {
        val id = stateMutex.withLock {
            ensureAcceptingTasks()

            scheduleTasks.addTask(
                task,
                startDelay = max(0L, delayFirstExecute.duration),
                delay = max(0L, delayBetweenExecute.duration)
            )
        }

        return DefaultTask(id, false, this)
    }

    override fun cancelTask(taskId: Int): Boolean {
        return stateMutex.withLock {
            scheduleTasks.removeTask(taskId)
        }
    }

    override fun shutdown(cancelPendingTasks: Boolean) {
        stateMutex.withLock {
            acceptingTasks = false

            if (cancelPendingTasks) {
                stopped = true

                nowTasks.cleanup()
                laterTasks.cleanup()
                scheduleTasks.cleanup()
            }
        }
    }

    private fun ensureAcceptingTasks() {
        if (!acceptingTasks) {
            throw SchedulerClosedException()
        }
    }

    private fun durationToTicks(duration: Duration): Ticks {
        if (duration < minPrecisionUnit && duration > Duration.ZERO) {
            throw IllegalArgumentException(
                "Duration $duration smaller than precision $minPrecisionUnit"
            )
        }

        return duration.toTicks(minPrecisionUnit)
    }
}
