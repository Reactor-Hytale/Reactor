package ink.reactor.kernel.plugin.model.version

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VersionTest {

    @Test
    fun testParseValid() {
        val v = Version.parse("1.2.3")
        assertEquals(1, v.major)
        assertEquals(2, v.minor)
        assertEquals(3, v.patch)
        assertEquals(ReleaseChannel.STABLE, v.releaseChannel)
    }

    @Test
    fun testParseWithChannel() {
        val v = Version.parse("1.0.0-ALPHA")
        assertEquals(1, v.major)
        assertEquals(0, v.minor)
        assertEquals(0, v.patch)
        assertEquals(ReleaseChannel.ALPHA, v.releaseChannel)
    }

    @Test
    fun testParseWithDifferentChannels() {
        assertEquals(ReleaseChannel.BETA, Version.parse("2.1.0-BETA").releaseChannel)
        assertEquals(ReleaseChannel.RC, Version.parse("0.5.1-RC").releaseChannel)
        assertEquals(ReleaseChannel.PRE_RELEASE, Version.parse("3.0.0-PRE_RELEASE").releaseChannel)
    }

    @Test
    fun testParseInvalid() {
        assertThrows<IllegalArgumentException> { Version.parse("invalid") }
        assertThrows<IllegalArgumentException> { Version.parse("1.2") }
        assertThrows<IllegalArgumentException> { Version.parse("1.2.3.4") }
        assertThrows<IllegalArgumentException> { Version.parse("a.b.c") }
        assertThrows<IllegalArgumentException> { Version.parse("") }
    }

    @Test
    fun testInitValidation() {
        assertThrows<IllegalArgumentException> { Version(-1, 0, 0) }
        assertThrows<IllegalArgumentException> { Version(0, -1, 0) }
        assertThrows<IllegalArgumentException> { Version(0, 0, -1) }
    }

    @Test
    fun testCompareTo() {
        val v1 = Version(1, 0, 0)
        val v2 = Version(1, 0, 1)
        val v3 = Version(1, 1, 0)
        val v4 = Version(2, 0, 0)

        assertTrue(v1 < v2)
        assertTrue(v2 < v3)
        assertTrue(v3 < v4)
        assertTrue(v4 > v1)
    }

    @Test
    fun testCompareToWithChannels() {
        val stable = Version(1, 0, 0, ReleaseChannel.STABLE)
        val beta = Version(1, 0, 0, ReleaseChannel.BETA)
        val alpha = Version(1, 0, 0, ReleaseChannel.ALPHA)

        assertTrue(stable > beta)
        assertTrue(beta > alpha)
        assertTrue(alpha < stable)
    }

    @Test
    fun testToString() {
        assertEquals("1.0.0", Version(1, 0, 0).toString())
        assertEquals("1.0.0-ALPHA", Version(1, 0, 0, ReleaseChannel.ALPHA).toString())
        assertEquals("2.3.4-BETA", Version(2, 3, 4, ReleaseChannel.BETA).toString())
        assertEquals("0.1.0-RC", Version(0, 1, 0, ReleaseChannel.RC).toString())
        assertEquals("1.2.3-PRE_RELEASE", Version(1, 2, 3, ReleaseChannel.PRE_RELEASE).toString())
    }
}
