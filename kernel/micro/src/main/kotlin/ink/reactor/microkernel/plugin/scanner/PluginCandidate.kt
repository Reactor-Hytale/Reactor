package ink.reactor.microkernel.plugin.scanner

import ink.reactor.kernel.plugin.model.PluginId
import ink.reactor.kernel.plugin.model.PluginMetadata
import ink.reactor.microkernel.plugin.manifest.PluginManifest
import java.io.File

class PluginCandidate(
    val jarFile: File,
    val manifest: PluginManifest
) {
    val id: PluginId
        get() = manifest.metadata.id

    val metadata: PluginMetadata
        get() = manifest.metadata
}
