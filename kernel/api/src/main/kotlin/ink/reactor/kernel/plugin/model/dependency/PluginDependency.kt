package ink.reactor.kernel.plugin.model.dependency

import ink.reactor.kernel.plugin.model.PluginId
import ink.reactor.kernel.plugin.model.version.Version

data class PluginDependency(
    val id: PluginId,
    val version: Version,
    val kind: DependencyKind = DependencyKind.REQUIRED
)
