package ink.reactor.kernel.plugin.model.version

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReleaseChannelTest {

    @Test
    fun testParseStable() {
        assertEquals(ReleaseChannel.STABLE, ReleaseChannel.parse("stable"))
        assertEquals(ReleaseChannel.STABLE, ReleaseChannel.parse("STABLE"))
        assertEquals(ReleaseChannel.STABLE, ReleaseChannel.parse(" Stable "))
    }

    @Test
    fun testParseAlpha() {
        assertEquals(ReleaseChannel.ALPHA, ReleaseChannel.parse("alpha"))
        assertEquals(ReleaseChannel.ALPHA, ReleaseChannel.parse("ALPHA"))
    }

    @Test
    fun testParseBeta() {
        assertEquals(ReleaseChannel.BETA, ReleaseChannel.parse("beta"))
        assertEquals(ReleaseChannel.BETA, ReleaseChannel.parse("BETA"))
    }

    @Test
    fun testParseRc() {
        assertEquals(ReleaseChannel.RC, ReleaseChannel.parse("rc"))
        assertEquals(ReleaseChannel.RC, ReleaseChannel.parse("RC"))
        assertEquals(ReleaseChannel.RC, ReleaseChannel.parse("release_candidate"))
        assertEquals(ReleaseChannel.RC, ReleaseChannel.parse("RELEASE-CANDIDATE"))
    }

    @Test
    fun testParsePreRelease() {
        assertEquals(ReleaseChannel.PRE_RELEASE, ReleaseChannel.parse("pre_release"))
        assertEquals(ReleaseChannel.PRE_RELEASE, ReleaseChannel.parse("prerelease"))
        assertEquals(ReleaseChannel.PRE_RELEASE, ReleaseChannel.parse("pre"))
        assertEquals(ReleaseChannel.PRE_RELEASE, ReleaseChannel.parse("PRE-RELEASE"))
    }

    @Test
    fun testParseInvalid() {
        assertThrows<IllegalArgumentException> { ReleaseChannel.parse("invalid") }
        assertThrows<IllegalArgumentException> { ReleaseChannel.parse("") }
    }

    @Test
    fun testStabilityRank() {
        assertEquals(0, ReleaseChannel.PRE_RELEASE.stabilityRank())
        assertEquals(1, ReleaseChannel.ALPHA.stabilityRank())
        assertEquals(2, ReleaseChannel.BETA.stabilityRank())
        assertEquals(3, ReleaseChannel.RC.stabilityRank())
        assertEquals(4, ReleaseChannel.STABLE.stabilityRank())
    }

    @Test
    fun testCompareStability() {
        assertTrue(ReleaseChannel.STABLE.compareStability(ReleaseChannel.ALPHA) > 0)
        assertTrue(ReleaseChannel.ALPHA.compareStability(ReleaseChannel.STABLE) < 0)
        assertEquals(0, ReleaseChannel.ALPHA.compareStability(ReleaseChannel.ALPHA))
    }

    @Test
    fun testIsProductionReady() {
        assertTrue(ReleaseChannel.STABLE.isProductionReady)
        assertFalse(ReleaseChannel.PRE_RELEASE.isProductionReady)
        assertFalse(ReleaseChannel.ALPHA.isProductionReady)
        assertFalse(ReleaseChannel.BETA.isProductionReady)
        assertFalse(ReleaseChannel.RC.isProductionReady)
    }

    @Test
    fun testIsPrerelease() {
        assertTrue(ReleaseChannel.PRE_RELEASE.isPrerelease)
        assertTrue(ReleaseChannel.ALPHA.isPrerelease)
        assertTrue(ReleaseChannel.BETA.isPrerelease)
        assertTrue(ReleaseChannel.RC.isPrerelease)
        assertFalse(ReleaseChannel.STABLE.isPrerelease)
    }
}
