package codes.reactor.microkernel.plugin.manifest

import java.util.jar.JarFile

fun interface PluginManifestReader {
    fun read(jarFile: JarFile): PluginManifest
}
