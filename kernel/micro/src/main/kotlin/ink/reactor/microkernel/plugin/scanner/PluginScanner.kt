package codes.reactor.microkernel.plugin.scanner

import codes.reactor.kernel.logger.Logger
import codes.reactor.microkernel.plugin.manifest.PluginManifestReader
import java.io.File
import java.util.jar.JarFile

internal class PluginScanner(
    private val manifestReader: PluginManifestReader,
    private val logger: Logger
) {
    fun scan(directory: File): List<PluginCandidate> {
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }

        val candidates = ArrayList<PluginCandidate>()
        for (file in directory.listFiles() ?: return emptyList()) {
            if (!file.isFile || file.extension != "jar") {
                continue
            }

            try {
                JarFile(file).use { jar ->
                    candidates.add(PluginCandidate(file, manifestReader.read(jar)))
                }
            } catch (e: Exception) {
                logger.error("Error on scan the file ${file.name}", e)
            }
        }

        return candidates
    }
}
