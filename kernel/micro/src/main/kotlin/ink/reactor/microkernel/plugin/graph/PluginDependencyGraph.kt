package ink.reactor.microkernel.plugin.graph

import ink.reactor.kernel.plugin.exception.PluginDependencyException
import ink.reactor.kernel.plugin.model.PluginId

internal class PluginDependencyGraph {
    private val outgoing: MutableMap<PluginId, MutableSet<PluginId>> = linkedMapOf()
    private val incoming: MutableMap<PluginId, MutableSet<PluginId>> = linkedMapOf()

    fun addNode(id: PluginId) {
        outgoing.computeIfAbsent(id) { linkedSetOf() }
        incoming.computeIfAbsent(id) { linkedSetOf() }
    }

    fun addEdge(from: PluginId, to: PluginId) {
        addNode(from)
        addNode(to)
        outgoing.getValue(from).add(to)
        incoming.getValue(to).add(from)
    }

    fun nodes(): Set<PluginId> = outgoing.keys

    /**
     * Treats the directed dependency graph as undirected in order to find
     * independent components. Each component can be loaded in parallel because
     * no required edge crosses the component boundary.
     */
    fun connectedComponents(): List<Set<PluginId>> {
        val visited = linkedSetOf<PluginId>()
        val components = mutableListOf<Set<PluginId>>()

        for (start in nodes()) {
            if (!visited.add(start)) {
                continue
            }

            val component = linkedSetOf<PluginId>()
            val queue = ArrayDeque<PluginId>()
            queue.add(start)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                component += current

                val neighbours = linkedSetOf<PluginId>()
                neighbours += outgoing[current].orEmpty()
                neighbours += incoming[current].orEmpty()

                for (neighbour in neighbours) {
                    if (visited.add(neighbour)) {
                        queue.add(neighbour)
                    }
                }
            }

            components += component
        }

        return components
    }

    /**
     * Kahn topological order.
     *
     * Edges are dependency -> dependent, so the resulting order guarantees that
     * a required dependency appears before its dependents.
     */
    fun topologicalOrder(): List<PluginId> {
        val inDegree = nodes()
            .associateWith { incoming[it]?.size ?: 0 }
            .toMutableMap()

        val ready = ArrayDeque<PluginId>()
        inDegree
            .filterValues { it == 0 }
            .keys
            .forEach { ready.add(it) }

        val ordered = mutableListOf<PluginId>()

        while (ready.isNotEmpty()) {
            val node = ready.removeFirst()
            ordered += node

            for (dependent in outgoing[node].orEmpty()) {
                val newDegree = inDegree.getValue(dependent) - 1
                inDegree[dependent] = newDegree

                if (newDegree == 0) {
                    ready.add(dependent)
                }
            }
        }

        if (ordered.size != nodes().size) {
            val cyclicNodes = nodes() - ordered.toSet()
            throw PluginDependencyException(
                "Cyclic required plugin dependencies detected: ${cyclicNodes.joinToString(", ")}."
            )
        }

        return ordered
    }
}
