package ink.reactor.microkernel.plugin.validation

import ink.reactor.kernel.plugin.exception.PluginDependencyException
import ink.reactor.kernel.plugin.model.PluginId
import ink.reactor.kernel.plugin.model.PluginMetadata
import ink.reactor.kernel.plugin.model.dependency.DependencyKind
import ink.reactor.kernel.plugin.model.dependency.PluginDependency
import ink.reactor.microkernel.plugin.scanner.PluginCandidate

internal class PluginDependencyValidator {

    fun validateNewCandidates(
        candidates: List<PluginCandidate>,
        existingPlugins: Map<PluginId, PluginMetadata> = emptyMap()
    ): Map<PluginId, PluginCandidate> {
        validateDuplicateCandidates(candidates)
        validateAgainstExisting(candidates, existingPlugins)

        val candidatesById = candidates.associateBy { it.id }
        val allPlugins = LinkedHashMap<PluginId, PluginMetadata>()
        allPlugins += existingPlugins
        allPlugins += candidates.associate { it.id to it.metadata }

        validateDependencies(
            owners = candidates.map { it.metadata },
            availablePlugins = allPlugins
        )

        return candidatesById
    }

    fun validateInstalledPlugins(plugins: Collection<PluginMetadata>) {
        val available = plugins.associateBy { it.id }
        validateDependencies(
            owners = plugins,
            availablePlugins = available
        )
    }

    private fun validateDuplicateCandidates(candidates: List<PluginCandidate>) {
        val duplicates = candidates
            .groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys

        if (duplicates.isNotEmpty()) {
            throw PluginDependencyException(
                "Duplicate plugin ids in node: ${duplicates.joinToString(", ")}."
            )
        }
    }

    private fun validateAgainstExisting(
        candidates: List<PluginCandidate>,
        existingPlugins: Map<PluginId, PluginMetadata>
    ) {
        val collisions = candidates
            .map { it.id }
            .filter { it in existingPlugins }
            .toSet()

        if (collisions.isNotEmpty()) {
            throw PluginDependencyException(
                "Plugins are already registered: ${collisions.joinToString(", ")}."
            )
        }
    }

    private fun validateDependencies(
        owners: Collection<PluginMetadata>,
        availablePlugins: Map<PluginId, PluginMetadata>
    ) {
        val errors = mutableListOf<String>()

        for (owner in owners) {
            for (dependency in owner.requiredDependencies) {
                validateDependency(owner, dependency, availablePlugins, DependencyKind.REQUIRED, errors)
            }

            for (dependency in owner.optionalDependencies) {
                validateDependency(owner, dependency, availablePlugins, DependencyKind.OPTIONAL, errors)
            }
        }

        if (errors.isNotEmpty()) {
            throw PluginDependencyException(
                buildString {
                    appendLine("Plugin dependency validation failed:")
                    errors.forEach { appendLine("- $it") }
                }
            )
        }
    }

    private fun validateDependency(
        owner: PluginMetadata,
        dependency: PluginDependency,
        availablePlugins: Map<PluginId, PluginMetadata>,
        kind: DependencyKind,
        errors: MutableList<String>
    ) {
        val dependencyMetadata = availablePlugins[dependency.id]

        if (dependencyMetadata == null) {
            if (kind != DependencyKind.OPTIONAL) {
                errors += "${owner.id} requires missing dependency ${dependency.id} >= ${dependency.version}"
            }
            return
        }

        if (dependencyMetadata.version < dependency.version) {
            errors += "${owner.id} requires ${kind.name} dependency ${dependency.id} >= ${dependency.version}, " +
                "but found ${dependencyMetadata.version}"
        }
    }
}
