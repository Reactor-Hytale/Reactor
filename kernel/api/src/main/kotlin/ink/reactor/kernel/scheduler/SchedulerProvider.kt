package ink.reactor.kernel.scheduler

import ink.reactor.kernel.scheduler.tick.TickDrivenScheduler
import ink.reactor.kernel.scheduler.tick.Ticks
import kotlin.time.Duration

interface SchedulerProvider {

    fun createTickDriven(initialTick: Ticks, tickDuration: Duration): TickDrivenScheduler

    fun createTickDriven(tickDuration: Duration): TickDrivenScheduler {
        return createTickDriven(Ticks.ZERO, tickDuration)
    }
}
