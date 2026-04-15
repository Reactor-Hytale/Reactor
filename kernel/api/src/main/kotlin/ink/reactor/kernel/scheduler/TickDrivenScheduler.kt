package ink.reactor.kernel.scheduler

import kotlin.time.Duration

interface TickDrivenScheduler : Scheduler {
    var currentTick: Ticks
    var tickDuration: Duration

    fun tick()
    fun tick(amount: Ticks)

}
