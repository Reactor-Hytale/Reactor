package codes.reactor.gradle.task

import codes.reactor.gradle.scanner.PluginClassScanner
import codes.reactor.gradle.validation.Validator
import codes.reactor.gradle.writer.PluginPropertiesWriter
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import kotlin.collections.forEach

@CacheableTask
abstract class GeneratePluginMetadataTask : DefaultTask() {

    @get:Classpath
    abstract val classesDirs: ConfigurableFileCollection

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val includeSubpackages: Property<Boolean>

    @get:Input
    abstract val pluginAnnotationClass: Property<String>

    @get:Input
    abstract val bootstrapAnnotationClass: Property<String>

    @get:Input
    abstract val bootstrapAnnotationAliasClass: Property<String>

    @get:Input
    abstract val outputFileName: Property<String>

    @get:Input
    abstract val failOnMultiplePlugins: Property<Boolean>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val targetPackage = packageName.orNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: throw GradleException(
                "reactorPluginMetadata.packageName is blank and project.group is not a valid package"
            )

        val scanner = PluginClassScanner(
            pluginAnnotationClass.get(),
            bootstrapAnnotationClass.get(),
            bootstrapAnnotationAliasClass.get()
        )

        val scanResult = scanner.scan(
            classesDirs.files,
            targetPackage,
            includeSubpackages.get()
        )

        if (scanResult.pluginClass == null) {
            logger.lifecycle("No @Plugin annotation found in package '{}'. Nothing to generate.", targetPackage)
            cleanOutputDirectory()
            return
        }

        if (failOnMultiplePlugins.get() && scanResult.allPluginClasses.size > 1) {
            throw GradleException(
                buildString {
                    appendLine("Multiple @Plugin annotations found in package '$targetPackage'.")
                    appendLine("Matches:")
                    scanResult.allPluginClasses.forEach { appendLine(" - ${it.className}") }
                }
            )
        }

        val pluginData = scanResult.pluginClass.pluginData
            ?: throw GradleException("Internal error: @Plugin class was found but no annotation data was extracted")

        Validator.validatePluginData(pluginData, scanResult.pluginClass.className)

        val outputFile = outputDirectory.get().asFile.resolve(outputFileName.get())
        PluginPropertiesWriter().write(
            outputFile,
            pluginData,
            scanResult.pluginClass.className,
            scanResult.bootstrapClass?.className
        )

        logger.lifecycle(
            "Generated {} from @Plugin={}{}",
            outputFile.absolutePath,
            scanResult.pluginClass.className,
            scanResult.bootstrapClass?.let { ", @Boostrap=${it.className}" } ?: ""
        )
    }

    private fun cleanOutputDirectory() {
        val outputDir = outputDirectory.asFile.orNull ?: return
        if (outputDir.exists()) {
            outputDir.deleteRecursively()
        }
    }
}
