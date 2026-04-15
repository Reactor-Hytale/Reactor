package ink.reactor.kernel.plugin.query

import ink.reactor.kernel.plugin.model.PluginId
import ink.reactor.kernel.plugin.model.PluginSnapshot

interface PluginCatalog {
    operator fun get(id: PluginId): PluginSnapshot?
    fun findAll(): Collection<PluginSnapshot>
    fun contains(id: PluginId): Boolean
}
