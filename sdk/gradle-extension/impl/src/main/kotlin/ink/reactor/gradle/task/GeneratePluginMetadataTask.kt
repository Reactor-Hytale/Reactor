package ink.reactor.gradle.task

import ink.reactor.gradle.model.DependencyAnnotationData
import ink.reactor.gradle.model.PluginAnnotationData
import ink.reactor.gradle.scanner.PluginClassScanner
import ink.reactor.gradle.writer.PluginPropertiesWriter
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

        validatePluginData(pluginData, scanResult.pluginClass.className)

        val outputFile = outputDirectory.get().asFile.resolve(outputFileName.get())
        PluginPropertiesWriter().write(
            outputFile,
            pluginData,
            scanResult.bootstrapClass?.className
        )

        logger.lifecycle(
            "Generated {} from @Plugin={}{}",
            outputFile.absolutePath,
            scanResult.pluginClass.className,
            scanResult.bootstrapClass?.let { ", @Boostrap=${it.className}" } ?: ""
        )
    }

    private fun validatePluginData(plugin: PluginAnnotationData, ownerClassName: String) {
        if (plugin.id.isBlank()) {
            throw GradleException("@Plugin.id cannot be blank on $ownerClassName")
        }
        if (plugin.version.isBlank()) {
            throw GradleException("@Plugin.version cannot be blank on $ownerClassName")
        }

        validateDependencies("Required Dependency", ownerClassName, plugin.dependencies)
        validateDependencies("Soft Dependency", ownerClassName, plugin.dependencies)
    }

    private fun validateDependencies(
        type: String,
        ownerClassName: String,
        dependencies: MutableList<DependencyAnnotationData>
    ) {
        dependencies.forEach { dependency ->
            if (dependency.id.isBlank()) {
                throw GradleException("$type id cannot be blank on $ownerClassName")
            }
            if (dependency.version.isBlank()) {
                throw GradleException(
                    "$type '${dependency.id}' on $ownerClassName has blank version. " +
                        "The current runtime deserializer expects 'pluginId:version'."
                )
            }
        }
    }

    private fun cleanOutputDirectory() {
        val outputDir = outputDirectory.asFile.orNull ?: return
        if (outputDir.exists()) {
            outputDir.deleteRecursively()
        }
    }
}
