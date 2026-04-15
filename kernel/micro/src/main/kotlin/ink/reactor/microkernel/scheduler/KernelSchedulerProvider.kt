package ink.reactor.microkernel.scheduler

import ink.reactor.kernel.scheduler.SchedulerProvider
import ink.reactor.kernel.scheduler.TickDrivenScheduler
import ink.reactor.kernel.scheduler.Ticks
import kotlin.time.Duration

class KernelSchedulerProvider: SchedulerProvider {
    override fun createTickDriven(initialTick: Ticks, period: Duration): TickDrivenScheduler {
        return TickScheduler(initialTick, period)
    }
}
