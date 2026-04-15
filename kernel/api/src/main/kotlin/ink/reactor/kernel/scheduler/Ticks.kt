package ink.reactor.kernel.scheduler

import kotlin.time.Duration

@JvmInline
value class Ticks(val duration: Long) : Comparable<Ticks> {
    init {
        require(duration >= 0) { "Ticks cannot be negative." }
    }

    companion object {
        val ZERO = Ticks(0)

        fun builder(tickDuration: Duration) = Builder(tickDuration)
    }

    class Builder(val tickDuration: Duration) {
        private var duration: Long = 0

        fun fromHours(hours: Long) = apply { duration = (hours * 60 * 60 * 1000) / tickDuration.inWholeMilliseconds }
        fun fromMinutes(minutes: Long) = apply { duration = (minutes * 60 * 1000) / tickDuration.inWholeMilliseconds }
        fun fromSeconds(seconds: Long) = apply { duration = (seconds * 1000) / tickDuration.inWholeMilliseconds }
        fun fromMillis(millis: Long) = apply { duration = millis / tickDuration.inWholeMilliseconds }
        fun fromDuration(duration: Duration) = apply { this.duration = duration.inWholeMilliseconds / tickDuration.inWholeMilliseconds }

        fun build() = Ticks(duration)
    }

    operator fun plus(other: Ticks) = Ticks(duration + other.duration)
    operator fun minus(other: Ticks) = Ticks(duration - other.duration)
    override fun compareTo(other: Ticks) = duration.compareTo(other.duration)
}
