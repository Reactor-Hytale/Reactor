package codes.reactor.microkernel.plugin.catalog

import codes.reactor.kernel.plugin.exception.PluginDependencyException
import codes.reactor.kernel.plugin.model.PluginId
import codes.reactor.kernel.plugin.model.PluginSnapshot
import codes.reactor.kernel.plugin.model.lifecycle.PluginState
import codes.reactor.kernel.plugin.query.PluginCatalog
import codes.reactor.microkernel.plugin.classloading.PluginClassLoader
import codes.reactor.microkernel.plugin.scanner.PluginCandidate

internal class DefaultPluginCatalog : PluginCatalog {
    private val lock = Any()
    private val plugins = linkedMapOf<PluginId, PluginEntry>()

    fun register(candidate: PluginCandidate, state: PluginState): PluginEntry {
        synchronized(lock) {
            val id = candidate.manifest.metadata.id
            if (plugins.containsKey(id)) {
                throw PluginDependencyException("Plugin '$id' is already registered.")
            }

            val entry = PluginEntry(candidate, state)
            plugins[id] = entry
            return entry
        }
    }

    fun entry(id: PluginId): PluginEntry? {
        synchronized(lock) {
            return plugins[id]
        }
    }

    fun entries(): List<PluginEntry> {
        synchronized(lock) {
            return plugins.values.toList()
        }
    }

    override operator fun get(id: PluginId): PluginSnapshot? {
        return entry(id)?.snapshot()
    }

    override fun getFromCurrentScope(): PluginSnapshot? {
        val loader = Thread.currentThread().contextClassLoader
        if (loader is PluginClassLoader) {
            return get(loader.id)
        }
        return null
    }

    override fun findAll(): Collection<PluginSnapshot> {
        return entries().map { it.snapshot() }
    }

    override fun contains(id: PluginId): Boolean {
        synchronized(lock) {
            return plugins.containsKey(id)
        }
    }
}
