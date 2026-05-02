package codes.reactor.microkernel.plugin.manifest

import codes.reactor.kernel.plugin.model.PluginMetadata

class PluginManifest(
    val metadata: PluginMetadata,
    val mainClass: String,
    val boostrapClass: String?
)
