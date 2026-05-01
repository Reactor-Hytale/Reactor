package ink.reactor.microkernel.scheduler

import ink.reactor.kernel.scheduler.tick.toTicks
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TicksConverterTest {

    @Test
    fun testConversion() {
        val tickDuration = 50.milliseconds
        assertEquals(0, 12.milliseconds.toTicks(tickDuration).duration)
        assertEquals(1, 50.milliseconds.toTicks(tickDuration).duration)
        assertEquals(20, 1.seconds.toTicks(tickDuration).duration)
        assertEquals(20 * 60, 1.minutes.toTicks(tickDuration).duration)
        assertEquals(20 * 60 * 60, 1.hours.toTicks(tickDuration).duration)
    }
}
