package ink.reactor.microkernel.scheduler

import ink.reactor.kernel.scheduler.TickDrivenScheduler
import ink.reactor.kernel.scheduler.Ticks
import ink.reactor.kernel.scheduler.exception.SchedulerClosedException
import kotlin.math.max
import kotlin.time.Duration

class TickScheduler(
    override var currentTick: Ticks,
    override var tickDuration: Duration
) : TickDrivenScheduler {

    private var nowTasks: NowTasks? = NowTasks()
    private var laterTasks: LaterTasks? = LaterTasks()
    private var scheduleTasks: ScheduleTasks? = ScheduleTasks()

    override fun tick() {
        nowTasks?.executeAll()
        laterTasks?.executeAll()
        scheduleTasks?.executeAll()
    }

    override fun tick(amount: Ticks) {
        var i = 0
        while (i++ < amount.duration) {
            tick()
        }
    }

    override fun runNow(task: () -> Unit) {
        nowTasks?.addTask(task)
    }

    override fun runAtTick(task: () -> Unit, tickToExecute: Ticks) {
        if (tickToExecute.duration - 1 <= 0) {
            runNow(task)
            return
        }
        laterTasks?.addTask(task, tickToExecute.duration - 1)
    }

    override fun scheduleEvery(
        task: () -> Unit,
        tickToStart: Ticks,
        executeInTheTick: Ticks
    ): Int {
        val tickToStartValue = max(0, tickToStart.duration - 1)
        val delayBetween = max(0, executeInTheTick.duration - 1)
        return scheduleTasks?.addTask(task, tickToStartValue, delayBetween)
            ?: throw SchedulerClosedException()
    }

    override fun runAfterDelay(task: () -> Unit, delay: Ticks) {
        val ticks = delay.duration
        if (ticks <= 0) {
            nowTasks?.addTask(task)
            return
        }
        laterTasks?.addTask(task, delay.duration)
    }

    override fun scheduleWithDelayBetween(
        task: () -> Unit,
        delayFirstExecute: Ticks,
        delayBetweenExecute: Ticks
    ): Int {
        return scheduleTasks?.addTask(task, delayFirstExecute.duration, delayBetweenExecute.duration)
            ?: throw SchedulerClosedException()
    }

    override fun cancelScheduleTask(taskId: Int): Boolean {
        return scheduleTasks?.removeTask(taskId) ?: false
    }

    override fun shutdown(cancelPendingTasks: Boolean) {
        nowTasks?.cleanup()
        laterTasks?.cleanup()
        scheduleTasks?.cleanup()

        nowTasks = null
        laterTasks = null
        scheduleTasks = null
    }
}
