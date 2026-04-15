package ink.reactor.kernel.plugin.model.version

/**
 * Defines the release channel of a version.
 *
 * A release channel classifies the maturity and intended usage of a build,
 * ranging from unstable development snapshots to production-ready releases.
 *
 * Stability order from lowest to highest:
 * PRE_RELEASE < ALPHA < BETA < RC < STABLE
 */
enum class ReleaseChannel {
    /**
     * Development or snapshot build.
     *
     * These builds are highly volatile and may include incomplete or unstable code.
     * They are intended for development, CI, or very early validation only.
     */
    PRE_RELEASE,

    /**
     * Early testing release.
     *
     * Alpha versions are still under heavy development and may be incomplete
     * or unstable.
     */
    ALPHA,

    /**
     * Feature-complete testing release.
     *
     * Beta versions are intended for broader testing and feedback, but may
     * still contain bugs.
     */
    BETA,

    /**
     * Release candidate.
     *
     * RC builds are considered nearly production-ready and are used for final
     * validation before a stable release.
     */
    RC,

    /**
     * Production-ready release.
     *
     * Stable versions are intended for production usage and represent the
     * highest maturity level.
     */
    STABLE;

    /**
     * Whether this channel is considered production-ready.
     */
    val isProductionReady: Boolean
        get() = this == STABLE

    /**
     * Whether this channel represents a pre-release build.
     *
     * This includes all channels that precede a final stable release.
     */
    val isPrerelease: Boolean
        get() = this != STABLE

    /**
     * Returns a numeric stability rank where higher means more stable.
     */
    fun stabilityRank(): Int {
        return when (this) {
            PRE_RELEASE -> 0
            ALPHA -> 1
            BETA -> 2
            RC -> 3
            STABLE -> 4
        }
    }

    /**
     * Compares this release channel with another based on stability.
     *
     * @param other the other release channel
     * @return a negative value if this is less stable, zero if equal,
     * or a positive value if this is more stable
     */
    fun compareStability(other: ReleaseChannel): Int {
        return stabilityRank().compareTo(other.stabilityRank())
    }

    companion object {
        fun parse(raw: String): ReleaseChannel {
            return when (
                raw.trim()
                    .uppercase()
                    .replace('-', '_')
                    .replace(' ', '_')
            ) {
                "STABLE" -> STABLE
                "ALPHA" -> ALPHA
                "BETA" -> BETA
                "RC", "RELEASE_CANDIDATE" -> RC
                "PRE_RELEASE", "PRERELEASE", "PRE" -> PRE_RELEASE
                else -> throw IllegalArgumentException("Unknown release channel: $raw")
            }
        }
    }
}
