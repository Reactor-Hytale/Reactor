package ink.reactor.microkernel.scheduler

import ink.reactor.kernel.scheduler.SchedulerProvider
import ink.reactor.kernel.scheduler.tick.TickDrivenScheduler
import ink.reactor.kernel.scheduler.tick.Ticks
import ink.reactor.microkernel.scheduler.tick.TickScheduler
import kotlin.time.Duration

class KernelSchedulerProvider: SchedulerProvider {
    override fun createTickDriven(initialTick: Ticks, tickDuration: Duration): TickDrivenScheduler {
        return TickScheduler(initialTick, tickDuration)
    }
}
