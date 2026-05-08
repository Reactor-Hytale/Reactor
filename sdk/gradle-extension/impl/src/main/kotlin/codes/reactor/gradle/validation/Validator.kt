package codes.reactor.gradle.validation

import codes.reactor.gradle.model.DependencyAnnotationData
import codes.reactor.gradle.model.PluginAnnotationData
import org.gradle.api.GradleException
import kotlin.collections.forEach

object Validator {
    val ID_PATTERN = Regex("^[A-Z-a-z]*$")

    fun validatePluginData(plugin: PluginAnnotationData, ownerClassName: String) {
        if (plugin.id.isBlank()) {
            throw GradleException("@Plugin.id cannot be blank on $ownerClassName")
        }
        if (plugin.id.length > 32) {
            throw GradleException("Plugin id cannot be longer than 32 characters.")
        }
        if (!ID_PATTERN.matches(plugin.id)) {
            throw GradleException("Plugin id '${plugin.id}' only allows letters.")
        }

        if (plugin.version.isBlank()) {
            throw GradleException("@Plugin.version cannot be blank on $ownerClassName")
        }

        validateDependencies("Required Dependency", ownerClassName, plugin.dependencies)
        validateDependencies("Soft Dependency", ownerClassName, plugin.dependencies)
    }

    fun validateDependencies(
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
                    "$type '${dependency.id}' on $ownerClassName has blank version." +
                        "The current runtime deserializer expects 'pluginId:version'."
                )
            }
        }
    }
}
