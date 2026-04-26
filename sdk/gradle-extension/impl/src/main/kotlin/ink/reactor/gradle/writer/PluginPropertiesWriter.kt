package ink.reactor.gradle.writer

import ink.reactor.gradle.model.PluginAnnotationData
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class PluginPropertiesWriter {

    fun write(
        outputFile: File,
        plugin: PluginAnnotationData,
        main: String,
        bootstrapClassName: String?
    ) {
        outputFile.parentFile.mkdirs()

        val requiredDependencies = plugin.dependencies.joinToString(",") { "${it.id}:${it.version}" }
        val optionalDependencies = plugin.softDependencies.joinToString(",") { "${it.id}:${it.version}" }

        val content = buildString {
            appendKeyValue("main", main)
            appendKeyValue("id", plugin.id)
            appendKeyValue("version", plugin.version)

            if (plugin.description.isNotBlank()) {
                appendKeyValue("description", plugin.description)
            }

            if (plugin.authors.isNotEmpty()) {
                appendKeyValue("authors", plugin.authors.joinToString(","))
            }

            if (requiredDependencies.isNotBlank()) {
                appendKeyValue("dependencies", requiredDependencies)
            }

            if (optionalDependencies.isNotBlank()) {
                appendKeyValue("softDependencies", optionalDependencies)
            }

            if (!bootstrapClassName.isNullOrBlank()) {
                appendKeyValue("boostrap", bootstrapClassName)
            }
        }

        Files.writeString(outputFile.toPath(), content, StandardCharsets.UTF_8)
    }

    private fun StringBuilder.appendKeyValue(key: String, value: String) {
        append(key)
        append('=')
        append(value)
        append('\n')
    }
}
