package codes.reactor.kernel.plugin.library

data class Repository(
    val url: String,
    val name: String? = null
) {
    init {
        require(url.isNotBlank()) { "Repository url cannot be blank" }
        require(url.startsWith("https://")) { "Repository url must use HTTPS: $url" }
    }

    companion object {
        val MAVEN_CENTRAL = Repository(
            "https://repo.maven.apache.org/maven2",
            "Maven Central"
        )
        val SONATYPE_SNAPSHOTS = Repository(
            "https://oss.sonatype.org/content/repositories/snapshots",
            "Sonatype Snapshots"
        )
        val JITPACK = Repository(
            "https://jitpack.io",
            "JitPack"
        )

        operator fun invoke(url: String, name: String? = null): Repository {
            val cleanUrl = url.trim().let {
                when {
                    it.startsWith("https://") -> it
                    it.startsWith("http://") -> it.replaceFirst("http://", "https://")
                    else -> "https://$it"
                }
            }
            return Repository(cleanUrl, name)
        }
    }
}
