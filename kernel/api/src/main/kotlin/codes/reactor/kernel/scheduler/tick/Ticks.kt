package codes.reactor.kernel.scheduler.tick

import kotlin.time.Duration

@JvmInline
value class Ticks(val duration: Long) : Comparable<Ticks> {
    init {
        require(duration >= 0) { "Ticks cannot be negative." }
    }

    companion object {
        val ZERO = Ticks(0)

        fun fromDuration(duration: Duration, tickDuration: Duration): Ticks {
            return Ticks(duration.inWholeMilliseconds / tickDuration.inWholeMilliseconds)
        }
    }

    operator fun plus(other: Ticks) = Ticks(duration + other.duration)
    operator fun minus(other: Ticks) = Ticks(duration - other.duration)
    operator fun times(scalar: Long) = Ticks(duration * scalar)

    override fun compareTo(other: Ticks) = duration.compareTo(other.duration)

    override fun toString(): String = "Ticks($duration)"
}
