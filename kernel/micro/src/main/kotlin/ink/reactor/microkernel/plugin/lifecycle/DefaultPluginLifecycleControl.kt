package ink.reactor.microkernel.plugin.lifecycle

import ink.reactor.kernel.logger.Logger
import ink.reactor.kernel.plugin.control.PluginLifecycleControl
import ink.reactor.kernel.plugin.exception.PluginDependencyException
import ink.reactor.kernel.plugin.exception.PluginNotFoundException
import ink.reactor.kernel.plugin.model.PluginId
import ink.reactor.kernel.plugin.model.dependency.DependencyKind
import ink.reactor.kernel.plugin.model.lifecycle.PluginState
import ink.reactor.microkernel.config.KernelPluginConfig
import ink.reactor.microkernel.plugin.lifecycle.runtime.PluginInstanceCreator
import ink.reactor.microkernel.plugin.lifecycle.runtime.PluginRuntimeReleaser
import ink.reactor.microkernel.plugin.lifecycle.logger.PluginStartupLogController
import ink.reactor.microkernel.plugin.catalog.DefaultPluginCatalog
import ink.reactor.microkernel.plugin.catalog.PluginEntry
import ink.reactor.microkernel.plugin.graph.PluginLoadGraphBuilder
import ink.reactor.microkernel.plugin.library.PluginLibraryResolver

internal class DefaultPluginLifecycleControl(
    private val catalog: DefaultPluginCatalog,
    libraryResolver: PluginLibraryResolver,
    config: KernelPluginConfig,
    private val logger: Logger,
    parentClassLoader: ClassLoader
) : PluginLifecycleControl {

    private val startupLogs = PluginStartupLogController(config, logger)
    private val releaser = PluginRuntimeReleaser(logger, startupLogs)
    private val instanceCreator = PluginInstanceCreator(catalog, libraryResolver, logger, parentClassLoader)
    private val runner = PluginLifecycleRunner(instanceCreator, releaser, startupLogs, logger)

    override fun load(id: PluginId) {
        loadWithDependencies(id, keepStartupLogger = false)
    }

    override fun enable(id: PluginId) {
        enableWithDependencies(id)
    }

    override fun disable(id: PluginId) {
        disableWithDependents(id, linkedSetOf())
    }

    internal fun start(id: PluginId) {
        val entry = requireEntry(id)

        try {
            loadWithDependencies(id, keepStartupLogger = true)
            enableWithDependencies(id)
        } catch (error: PluginStartupCancelledException) {
            throw error
        } catch (error: Throwable) {
            if (entry.state != PluginState.FAILED && entry.state != PluginState.CANCELLED) {
                runner.fail(entry, PluginState.FAILED, error)
            }
            throw error
        }
    }

    internal fun shutdown() {
        val entries = catalog.entries()
            .filter { it.state == PluginState.ENABLED || it.state == PluginState.LOADED }

        val orderedIds = runCatching {
            PluginLoadGraphBuilder()
                .build(entries.map { it.metadata })
                .topologicalOrder()
                .asReversed()
        }.getOrElse {
            entries.map { it.id }.asReversed()
        }

        for (id in orderedIds) {
            runCatching { disable(id) }
                .onFailure { error -> logger.error("Could not disable plugin $id during shutdown.", error) }
        }
    }

    internal fun cancelStartup(id: PluginId, reason: String) {
        val entry = requireEntry(id)
        runner.requestCancel(
            entry = entry,
            phase = cancellationPhaseOf(entry),
            reason = reason
        )
    }

    private fun loadWithDependencies(id: PluginId, keepStartupLogger: Boolean) {
        val entry = requireEntry(id)

        for (dependency in entry.metadata.requiredDependencies) {
            val dependencyEntry = requireEntry(dependency.id)
            if (dependencyEntry.state == PluginState.SKIPPED || dependencyEntry.state == PluginState.CANCELLED) {
                throw PluginDependencyException("Plugin $id requires unavailable dependency ${dependency.id}.")
            }

            loadWithDependencies(dependency.id, keepStartupLogger = false)
        }

        runner.load(entry, keepStartupLogger)
    }

    private fun enableWithDependencies(id: PluginId) {
        val entry = requireEntry(id)

        for (dependency in entry.metadata.requiredDependencies) {
            val dependencyEntry = requireEntry(dependency.id)
            if (dependencyEntry.state == PluginState.SKIPPED || dependencyEntry.state == PluginState.CANCELLED) {
                throw PluginDependencyException("Plugin $id requires unavailable dependency ${dependency.id}.")
            }

            enableWithDependencies(dependency.id)

            if (dependencyEntry.state != PluginState.ENABLED) {
                throw PluginDependencyException(
                    "Plugin $id requires dependency ${dependency.id} to be enabled, but it is ${dependencyEntry.state}."
                )
            }
        }

        if (entry.state == PluginState.DISABLED || entry.state == PluginState.FAILED || entry.state == PluginState.CANCELLED) {
            runner.load(entry, keepStartupLogger = true)
        }

        if (entry.state == PluginState.WAITING_FOR_DEPENDENCIES || entry.state == PluginState.SKIPPED) {
            loadWithDependencies(id, keepStartupLogger = true)
        }

        runner.enable(entry)
    }

    private fun disableWithDependents(id: PluginId, visited: MutableSet<PluginId>) {
        if (!visited.add(id)) {
            return
        }

        val entry = requireEntry(id)

        for (dependent in directDependentsOf(id)) {
            if (dependent.state == PluginState.ENABLED || dependent.state == PluginState.LOADED) {
                disableWithDependents(dependent.id, visited)
            }
        }

        runner.disable(entry)
    }

    private fun directDependentsOf(id: PluginId): List<PluginEntry> {
        return catalog.entries().filter { entry ->
            entry.metadata.dependencies.any { it.id == id && it.kind == DependencyKind.REQUIRED }
        }
    }

    private fun cancellationPhaseOf(entry: PluginEntry): PluginState {
        return synchronized(entry) {
            when (entry.state) {
                PluginState.ENABLING -> PluginState.ENABLING
                PluginState.LOADING -> PluginState.LOADING
                else -> PluginState.CANCELLED
            }
        }
    }

    private fun requireEntry(id: PluginId): PluginEntry {
        return catalog.entry(id) ?: throw PluginNotFoundException(id)
    }
}
