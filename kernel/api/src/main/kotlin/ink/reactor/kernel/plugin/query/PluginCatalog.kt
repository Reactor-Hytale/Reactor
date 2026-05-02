package codes.reactor.kernel.plugin.query

import codes.reactor.kernel.plugin.model.PluginId
import codes.reactor.kernel.plugin.model.PluginSnapshot

interface PluginCatalog {
    operator fun get(id: PluginId): PluginSnapshot?

    fun getFromCurrentScope(): PluginSnapshot?

    fun findAll(): Collection<PluginSnapshot>
    fun contains(id: PluginId): Boolean
}
