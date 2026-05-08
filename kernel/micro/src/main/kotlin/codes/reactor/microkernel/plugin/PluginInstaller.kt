package codes.reactor.microkernel.plugin

import codes.reactor.kernel.logger.Logger
import codes.reactor.kernel.plugin.exception.PluginDependencyException
import codes.reactor.kernel.plugin.model.PluginId
import codes.reactor.kernel.plugin.model.lifecycle.PluginState
import codes.reactor.microkernel.config.KernelPluginConfig
import codes.reactor.microkernel.plugin.catalog.DefaultPluginCatalog
import codes.reactor.microkernel.plugin.scanner.PluginCandidate
import codes.reactor.microkernel.plugin.scanner.PluginScanner
import codes.reactor.microkernel.plugin.validation.PluginDependencyValidator
import java.io.File

internal class PluginInstaller(
    private val config: KernelPluginConfig,
    private val catalog: DefaultPluginCatalog,
    private val scanner: PluginScanner,
    private val dependencyValidator: PluginDependencyValidator,
    private val logger: Logger
) {
    fun install(directory: File): List<PluginId> {
        val candidates = scanner.scan(directory)
        if (candidates.isEmpty()) {
            logger.info("No plugin jars found in ${directory.absolutePath}.")
            return emptyList()
        }

        validateDuplicateIds(candidates)

        val selectedCandidates = candidates.filter { isSelected(it) }
        dependencyValidator.validateNewCandidates(selectedCandidates)

        val selectedIds = selectedCandidates.mapTo(linkedSetOf()) { it.id }

        for (candidate in candidates) {
            val state = if (candidate.id in selectedIds) {
                PluginState.WAITING_FOR_DEPENDENCIES
            } else {
                PluginState.SKIPPED
            }
            catalog.register(candidate, state)
        }

        val skipped = candidates.size - selectedCandidates.size
        if (skipped > 0) {
            logger.info("Skipped $skipped plugin(s) by selection config.")
        }

        return selectedCandidates.map { it.id }
    }

    private fun isSelected(candidate: PluginCandidate): Boolean {
        val selection = config.selection
        if (selection.mode == KernelPluginConfig.Selection.Mode.ALL) {
            return true
        }

        val listed = selection.plugins.any { pluginFormat ->
            pluginFormat.id == candidate.id &&
                (pluginFormat.version == null || pluginFormat.version == candidate.metadata.version)
        }

        return when (selection.mode) {
            KernelPluginConfig.Selection.Mode.WHITELIST -> listed
            KernelPluginConfig.Selection.Mode.BLACKLIST -> !listed
        }
    }

    private fun validateDuplicateIds(candidates: List<PluginCandidate>) {
        val duplicates = candidates
            .groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys

        if (duplicates.isNotEmpty()) {
            throw PluginDependencyException(
                "Duplicate plugin ids in plugin directory: ${duplicates.joinToString(", ")}."
            )
        }
    }
}
