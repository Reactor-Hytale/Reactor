package codes.reactor.gradle.model

data class ScannedClassInfo(
    val className: String,
    val packageName: String,
    val hasPlugin: Boolean,
    val hasBootstrap: Boolean,
    val pluginData: PluginAnnotationData?
)
