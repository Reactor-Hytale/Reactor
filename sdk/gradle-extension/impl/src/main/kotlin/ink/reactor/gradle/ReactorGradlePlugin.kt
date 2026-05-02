package codes.reactor.gradle

import codes.reactor.gradle.extension.ReactorPluginMetadataExtension
import codes.reactor.gradle.task.GeneratePluginMetadataTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.register
import org.gradle.language.jvm.tasks.ProcessResources

class ReactorGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "reactorPluginMetadata",
            ReactorPluginMetadataExtension::class.java
        )

        project.pluginManager.withPlugin("java") {
            configureOnce(project, extension)
        }

        project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            configureOnce(project, extension)
        }
    }

    private fun configureOnce(
        project: Project,
        extension: ReactorPluginMetadataExtension
    ) {
        val key = "codes.reactor.gradle.plugin.configured"
        if (project.extensions.extraProperties.has(key)) {
            return
        }
        project.extensions.extraProperties.set(key, true)
        configure(project, extension)
    }

    private fun configure(
        project: Project,
        extension: ReactorPluginMetadataExtension
    ) {
        val defaultPackage = project.group.toString()
            .takeIf { it.isNotBlank() && it != "unspecified" }

        if (defaultPackage != null && !extension.packageName.isPresent) {
            extension.packageName.convention(defaultPackage)
        }

        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.getByName("main")

        val generateTask = project.tasks.register<GeneratePluginMetadataTask>("generatePluginMetadata") {
            group = "build"
            description = "Generates plugin.properties from @Plugin and @Boostrap annotations"

            classesDirs.from(main.output.classesDirs)
            packageName.set(extension.packageName)
            includeSubpackages.set(extension.includeSubpackages)
            pluginAnnotationClass.set(extension.pluginAnnotationClass)
            bootstrapAnnotationClass.set(extension.bootstrapAnnotationClass)
            bootstrapAnnotationAliasClass.set(extension.bootstrapAnnotationAliasClass)
            outputFileName.set(extension.outputFileName)
            failOnMultiplePlugins.set(extension.failOnMultiplePlugins)
            outputDirectory.set(
                project.layout.buildDirectory.dir("generated/resources/reactorPluginMetadata/main")
            )
        }

        project.tasks.withType(ProcessResources::class.java).configureEach {
            dependsOn(generateTask)
            from(generateTask.map { it.outputDirectory })
        }

        project.tasks.withType(Jar::class.java).configureEach {
            dependsOn(generateTask)
        }
    }
}
