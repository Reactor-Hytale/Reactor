package ink.reactor.microkernel.plugin

import ink.reactor.kernel.logger.Logger
import ink.reactor.kernel.plugin.model.PluginId
import ink.reactor.microkernel.config.KernelPluginConfig
import ink.reactor.microkernel.plugin.catalog.DefaultPluginCatalog
import ink.reactor.microkernel.plugin.graph.PluginDependencyGraph
import ink.reactor.microkernel.plugin.graph.PluginLoadGraphBuilder
import ink.reactor.microkernel.plugin.lifecycle.DefaultPluginLifecycleControl
import ink.reactor.microkernel.plugin.lifecycle.PluginStartupCancelledException
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

internal class PluginStarter(
    private val config: KernelPluginConfig,
    private val catalog: DefaultPluginCatalog,
    private val lifecycleControl: DefaultPluginLifecycleControl,
    private val logger: Logger
) {
    fun start(pluginIds: Collection<PluginId>) {
        if (pluginIds.isEmpty()) {
            return
        }

        val entries = pluginIds.mapNotNull { catalog.entry(it) }
        val graph = PluginLoadGraphBuilder().build(entries.map { it.metadata })

        if (config.loading.parallel) {
            startParallel(graph.connectedComponents())
            return
        }

        startSequence(graph)
    }

    private fun startSequence(graph: PluginDependencyGraph) {
        for (id in graph.topologicalOrder()) {
            try {
                lifecycleControl.start(id)
            } catch (_: PluginStartupCancelledException) {
                // Cancellation is controlled by PluginStarter/DefaultPluginLifecycleControl.
                // Do not print a full error stack for expected startup timeouts.
            } catch (error: Throwable) {
                logger.error("Plugin $id could not be started.", error)
            }
        }
    }

    private fun startParallel(components: Collection<Set<PluginId>>) {
        if (components.isEmpty()) {
            return
        }

        val executor = Executors.newVirtualThreadPerTaskExecutor()
        val timeoutInMillis = config.loading.timeOut.inWholeMilliseconds
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutInMillis)

        try {
            val futures = components.associateWith { component ->
                executor.submit(
                    Callable {
                        val componentGraph = PluginLoadGraphBuilder().build(
                            component.mapNotNull { catalog.entry(it)?.metadata }
                        )
                        startSequence(componentGraph)
                    }
                )
            }

            for ((component, future) in futures) {
                val remainingNanos = deadline - System.nanoTime()
                if (remainingNanos <= 0L) {
                    cancelComponent(component, future, timeoutInMillis)
                    continue
                }

                try {
                    future.get(remainingNanos, TimeUnit.NANOSECONDS)
                } catch (_: TimeoutException) {
                    cancelComponent(component, future, timeoutInMillis)
                } catch (_: CancellationException) {
                    // Already canceled by timeout or by another controlled path.
                } catch (error: ExecutionException) {
                    val cause = error.cause
                    if (cause !is PluginStartupCancelledException) {
                        logger.error("A plugin loading group $component failed.", cause ?: error)
                    }
                } catch (error: Exception) {
                    logger.error("A plugin loading group $component failed.", error)
                }
            }
        } finally {
            executor.shutdownNow()

            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Plugin executor did not terminate within timeout.")
            }
        }
    }

    private fun cancelComponent(
        component: Set<PluginId>,
        future: Future<*>,
        timeoutInMillis: Long
    ) {
        for (pluginId in component) {
            runCatching {
                lifecycleControl.cancelStartup(
                    pluginId,
                    "Plugin startup exceeded ${timeoutInMillis}ms."
                )
            }.onFailure { error ->
                logger.error("Could not request cancellation for plugin $pluginId after startup timeout.", error)
            }
        }

        future.cancel(true)

        logger.warn(
            "Plugin loading group $component took more than ${timeoutInMillis}ms. " +
                "It was cancelled and startup will continue."
        )
    }
}
