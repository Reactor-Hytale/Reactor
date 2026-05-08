package codes.reactor.microkernel.plugin.graph

import codes.reactor.kernel.plugin.model.PluginId
import codes.reactor.kernel.plugin.model.PluginMetadata

internal class PluginLoadGraphBuilder {
    fun build(plugins: Collection<PluginMetadata>): PluginDependencyGraph {
        val pluginsById = plugins.associateBy { it.id }
        return build(pluginsById)
    }

    fun build(pluginsById: Map<PluginId, PluginMetadata>): PluginDependencyGraph {
        val graph = PluginDependencyGraph()

        for (id in pluginsById.keys) {
            graph.addNode(id)
        }

        for (plugin in pluginsById.values) {
            for (dependency in plugin.requiredDependencies) {
                if (dependency.id in pluginsById) {
                    graph.addEdge(
                        from = dependency.id,
                        to = plugin.id
                    )
                }
            }
        }

        return graph
    }
}
