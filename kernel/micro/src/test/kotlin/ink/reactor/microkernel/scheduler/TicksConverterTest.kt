package ink.reactor.microkernel.scheduler

import ink.reactor.kernel.scheduler.Ticks
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class TicksConverterTest {

    @Test
    fun testConversion() {
        val ticks = Ticks.builder(50.milliseconds)
        assertEquals(0, ticks.fromMillis(12).build().duration)
        assertEquals(1, ticks.fromDuration(50.milliseconds).build().duration)
        assertEquals(20, ticks.fromSeconds(1).build().duration)
        assertEquals(20 * 60, ticks.fromMinutes(1).build().duration)
        assertEquals(20 * 60 * 60, ticks.fromHours(1).build().duration)
    }
}
