package codes.reactor.kernel.plugin.model

import codes.reactor.kernel.plugin.model.dependency.DependencyKind
import codes.reactor.kernel.plugin.model.dependency.PluginDependency
import codes.reactor.kernel.plugin.model.version.Version

data class PluginMetadata(
    val id: PluginId,
    val version: Version,
    val description: String? = null,
    val authors: Set<String> = emptySet(),
    val dependencies: Collection<PluginDependency> = emptySet()
) {
    val requiredDependencies: Set<PluginDependency>
        get() = dependencies.filterTo(linkedSetOf()) { it.kind == DependencyKind.REQUIRED }

    val optionalDependencies: Set<PluginDependency>
        get() = dependencies.filterTo(linkedSetOf()) { it.kind == DependencyKind.OPTIONAL }
}
