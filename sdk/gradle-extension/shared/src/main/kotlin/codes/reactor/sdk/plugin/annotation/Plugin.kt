package codes.reactor.sdk.plugin.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Plugin(
    val id: String,
    val version: String,
    val description: String = "",
    val authors: Array<String> = [],
    val dependencies: Array<Dependency> = [],
    val softDependencies: Array<Dependency> = []
)
