package codes.reactor.microkernel.plugin.scanner

import codes.reactor.kernel.plugin.model.PluginId
import codes.reactor.kernel.plugin.model.PluginMetadata
import codes.reactor.microkernel.plugin.manifest.PluginManifest
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
