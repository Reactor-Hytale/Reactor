package ink.reactor.kernel

import ink.reactor.kernel.logger.LoggerFactory
import ink.reactor.kernel.logger.LoggerSpy
import ink.reactor.kernel.plugin.control.PluginLifecycleControl
import ink.reactor.kernel.plugin.query.PluginCatalog
import ink.reactor.kernel.plugin.scope.PluginScopeFactory
import ink.reactor.kernel.scheduler.SchedulerProvider
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class Reactor private constructor(
    val loggerFactory: LoggerFactory,
    val loggerSpy: LoggerSpy,

    val schedulerProvider: SchedulerProvider,
    val pluginCatalog: PluginCatalog,
    val pluginLifecycleControl: PluginLifecycleControl,
    val pluginScopeFactory: PluginScopeFactory,

    internal val basePluginDirectory: Path,
    internal val baseLibraryDirectory: Path
) {

    companion object {
        @Volatile
        private var ref: Reactor? = null

        private val stopTasks = CopyOnWriteArrayList<() -> Unit>()
        private val shuttingDown = AtomicBoolean(false)

        val loggerFactory: LoggerFactory get() = instance.loggerFactory
        val loggerSpy: LoggerSpy get() = instance.loggerSpy

        val schedulerProvider: SchedulerProvider get() = instance.schedulerProvider

        val pluginCatalog: PluginCatalog get() = instance.pluginCatalog
        val pluginLifecycleControl: PluginLifecycleControl get() = instance.pluginLifecycleControl
        val pluginScopeFactory: PluginScopeFactory get() = instance.pluginScopeFactory

        val pluginDirectory: Path get() = instance.basePluginDirectory
        val libraryDirectory: Path get() = instance.baseLibraryDirectory

        fun init(
            loggerFactory: LoggerFactory,
            loggerSpy: LoggerSpy,
            schedulerProvider: SchedulerProvider,
            pluginCatalog: PluginCatalog,
            pluginLifecycleControl: PluginLifecycleControl,
            pluginScopeFactory: PluginScopeFactory,
            basePluginDirectory: Path,
            baseLibraryDirectory: Path
        ) {
            check(ref == null) { "Kernel already initialized." }

            ref = Reactor(
                loggerFactory, loggerSpy,
                schedulerProvider,
                pluginCatalog, pluginLifecycleControl, pluginScopeFactory,
                basePluginDirectory, baseLibraryDirectory
            )

            Runtime.getRuntime().addShutdownHook(Thread {
                shuttingDown.set(true)
                stopTasks.asReversed().forEach { task ->
                    runCatching(task).onFailure { error ->
                        System.err.println("Shutdown task failed.")
                        error.printStackTrace()
                    }
                }
            })
        }

        fun addStopTask(task: () -> Unit) {
            check(!shuttingDown.get()) { "Cannot register stop task while shutting down." }
            stopTasks += task
        }

        val instance: Reactor
            get() = ref ?: error("Kernel not initialized.")
    }
}
