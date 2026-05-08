package codes.reactor.microkernel.scheduler

import codes.reactor.kernel.scheduler.SchedulerProvider
import codes.reactor.kernel.scheduler.tick.TickDrivenScheduler
import codes.reactor.kernel.scheduler.tick.Ticks
import codes.reactor.microkernel.scheduler.tick.TickScheduler
import kotlin.time.Duration

class KernelSchedulerProvider: SchedulerProvider {
    override fun createTickDriven(initialTick: Ticks, tickDuration: Duration): TickDrivenScheduler {
        return TickScheduler(initialTick, tickDuration)
    }
}
