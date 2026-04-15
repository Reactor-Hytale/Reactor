package ink.reactor.kernel.scheduler

import kotlin.time.Duration

interface SchedulerProvider {

    fun createTickDriven(initialTick: Ticks, period: Duration): TickDrivenScheduler

    fun createTickDriven(tickDuration: Duration): TickDrivenScheduler {
        return createTickDriven(Ticks.ZERO, tickDuration)
    }
}
