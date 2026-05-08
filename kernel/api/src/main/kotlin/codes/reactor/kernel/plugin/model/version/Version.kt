package codes.reactor.kernel.plugin.model.version

/**
 * Represents a semantic plugin version with an optional release channel.
 *
 * Examples: 1.0.0, 1.0.0-ALPHA, 1.2.3-BETA, 2.0.0-RC, 0.4.0-PRE_RELEASE
 */
data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val releaseChannel: ReleaseChannel = ReleaseChannel.STABLE,
) : Comparable<Version> {

    init {
        require(major >= 0) { "major must be greater than or equal to 0" }
        require(minor >= 0) { "minor must be greater than or equal to 0" }
        require(patch >= 0) { "patch must be greater than or equal to 0" }
    }

    override fun compareTo(other: Version): Int {
        val coreComparison = compareValuesBy(
            this,
            other,
            Version::major,
            Version::minor,
            Version::patch,
        )

        if (coreComparison != 0) {
            return coreComparison
        }

        return releaseChannel.compareStability(other.releaseChannel)
    }

    override fun toString(): String {
        val base = "$major.$minor.$patch"

        return if (releaseChannel == ReleaseChannel.STABLE) {
            base
        } else {
            "$base-${releaseChannel.name}"
        }
    }

    companion object {
        private val REGEX = Regex(
            pattern = """^(\d+)\.(\d+)\.(\d+)(?:-([A-Za-z][A-Za-z0-9_\-]*))?$"""
        )

        fun parse(raw: String): Version {
            val normalized = raw.trim()
            val match = REGEX.matchEntire(normalized)
                ?: throw IllegalArgumentException("Invalid version: $raw")

            val (major, minor, patch, channelRaw) = match.destructured

            val releaseChannel = if (channelRaw.isBlank()) {
                ReleaseChannel.STABLE
            } else {
                ReleaseChannel.parse(channelRaw)
            }

            return Version(
                major = major.toInt(),
                minor = minor.toInt(),
                patch = patch.toInt(),
                releaseChannel = releaseChannel,
            )
        }
    }
}
