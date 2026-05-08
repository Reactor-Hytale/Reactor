package codes.reactor.kernel.plugin.library

data class Dependency(
    val alias: String,
    val group: String,
    val artifact: String,
    val version: String
) {
    init {
        require(alias.isNotBlank()) { "Dependency alias cannot be blank" }
        require(group.isNotBlank()) { "Dependency group cannot be blank" }
        require(artifact.isNotBlank()) { "Dependency artifact cannot be blank" }
        require(version.isNotBlank()) { "Dependency version cannot be blank" }
    }

    val notation: String
        get() = "$group:$artifact:$version"

    companion object {
        fun parse(notation: String, alias: String? = null): Dependency {
            require(notation.isNotBlank()) { "Dependency notation cannot be blank" }

            val parts = notation.split(':')
            require(parts.size == 3) {
                "Invalid Maven notation: '$notation'. Expected format: group:artifact:version"
            }

            val (group, artifact, version) = parts
            val finalAlias = alias ?: "$group:$artifact"

            return Dependency(finalAlias, group, artifact, version)
        }
    }
}
