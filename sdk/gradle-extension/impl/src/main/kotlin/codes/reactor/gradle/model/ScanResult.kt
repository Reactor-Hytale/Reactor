package codes.reactor.gradle.model

data class ScanResult(
    val pluginClass: ScannedClassInfo?,
    val bootstrapClass: ScannedClassInfo?,
    val allPluginClasses: List<ScannedClassInfo>
)
