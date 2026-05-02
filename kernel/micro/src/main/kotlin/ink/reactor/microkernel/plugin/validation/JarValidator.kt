package codes.reactor.microkernel.plugin.validation

import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile

object JarValidator {

    fun isValidJarFile(file: Path): Boolean {
        if (!Files.isRegularFile(file)) {
            return false
        }

        return runCatching {
            JarFile(file.toFile(), false).use { jar ->
                jar.entries().hasMoreElements()
            }
        }.getOrElse { false }
    }
}
