package ink.reactor.microkernel.plugin.manifest

import ink.reactor.kernel.plugin.model.PluginMetadata

class PluginManifest(
    val metadata: PluginMetadata,
    val mainClass: String,
    val boostrapClass: String?
)
