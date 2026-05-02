package codes.reactor.gradle.model

data class PluginAnnotationData(
    var id: String = "",
    var version: String = "",
    var description: String = "",
    val authors: MutableList<String> = mutableListOf(),
    val dependencies: MutableList<DependencyAnnotationData> = mutableListOf(),
    val softDependencies: MutableList<DependencyAnnotationData> = mutableListOf()
)
