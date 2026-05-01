package ink.reactor.kernel.scheduler.tick

import kotlin.time.Duration

fun Duration.toTicks(tickDuration: Duration): Ticks {
    require(!tickDuration.isNegative() && tickDuration != Duration.ZERO) { "Tick duration must be positive" }
    return Ticks(this.inWholeMilliseconds / tickDuration.inWholeMilliseconds)
}
