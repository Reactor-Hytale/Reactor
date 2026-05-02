package codes.reactor.kernel.plugin.library

class LibrariesRequest {

    private val repositories = linkedSetOf<Repository>()
    private val dependencies = linkedSetOf<Dependency>()

    val requestedRepositories: Set<Repository>
        get() = repositories.toSet()

    val requestedDependencies: Set<Dependency>
        get() = dependencies.toSet()

    fun repository(url: String) {
        repositories += Repository(url)
    }

    fun repository(repository: Repository) {
        repositories += repository
    }

    fun dependency(
        group: String,
        artifact: String,
        version: String,
        alias: String? = null
    ) {
        val finalAlias = alias ?: "$group:$artifact"

        require(dependencies.none { it.alias == finalAlias }) {
            "Dependency alias '$finalAlias' is already registered"
        }

        dependencies += Dependency(
            alias = finalAlias,
            group = group,
            artifact = artifact,
            version = version
        )
    }

    fun dependency(notation: String, alias: String? = null) {
        val dependency = Dependency.parse(notation, alias)

        require(dependencies.none { it.alias == dependency.alias }) {
            "Dependency alias '${dependency.alias}' is already registered"
        }

        dependencies += dependency
    }
}
