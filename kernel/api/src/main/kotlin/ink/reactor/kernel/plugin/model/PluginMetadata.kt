package ink.reactor.kernel.plugin.model

import ink.reactor.kernel.plugin.model.dependency.DependencyKind
import ink.reactor.kernel.plugin.model.dependency.PluginDependency
import ink.reactor.kernel.plugin.model.version.Version

data class PluginMetadata(
    val id: PluginId,
    val name: String,
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
