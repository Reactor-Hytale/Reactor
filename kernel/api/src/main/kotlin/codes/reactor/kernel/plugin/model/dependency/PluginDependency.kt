package codes.reactor.kernel.plugin.model.dependency

import codes.reactor.kernel.plugin.model.PluginId
import codes.reactor.kernel.plugin.model.version.Version

data class PluginDependency(
    val id: PluginId,
    val version: Version,
    val kind: DependencyKind = DependencyKind.REQUIRED
)
