package ink.reactor.microkernel.plugin.catalog

import ink.reactor.kernel.plugin.model.PluginId
import ink.reactor.kernel.plugin.model.PluginMetadata
import ink.reactor.kernel.plugin.model.PluginSnapshot
import ink.reactor.kernel.plugin.model.failure.PluginFailure
import ink.reactor.kernel.plugin.model.lifecycle.PluginState
import ink.reactor.kernel.plugin.spi.lifecycle.PluginLifecycle
import ink.reactor.microkernel.plugin.classloading.PluginClassLoader
import ink.reactor.microkernel.plugin.scanner.PluginCandidate

internal class PluginEntry(
    val candidate: PluginCandidate,
    initialState: PluginState
) {
    val id: PluginId
        get() = metadata.id

    val metadata: PluginMetadata
        get() = candidate.manifest.metadata

    @Volatile
    var state: PluginState = initialState

    @Volatile
    var failure: PluginFailure? = null

    @Volatile
    var lifecycle: PluginLifecycle? = null

    @Volatile
    var classLoader: PluginClassLoader? = null

    fun snapshot(): PluginSnapshot {
        return PluginSnapshot(state, failure, metadata)
    }

    override fun toString(): String = id.toString()
}
