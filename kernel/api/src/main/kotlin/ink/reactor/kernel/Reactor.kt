package ink.reactor.kernel

import ink.reactor.kernel.logger.LoggerFactory
import ink.reactor.kernel.plugin.control.PluginLifecycleControl
import ink.reactor.kernel.plugin.query.PluginCatalog
import ink.reactor.kernel.plugin.scope.PluginScopeFactory
import ink.reactor.kernel.scheduler.SchedulerProvider
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class Reactor private constructor(
    val loggerFactory: LoggerFactory,

    val schedulerProvider: SchedulerProvider,
    val pluginCatalog: PluginCatalog,
    val pluginLifecycleControl: PluginLifecycleControl,
    val pluginScopeFactory: PluginScopeFactory
) {

    companion object {
        @Volatile
        private var ref: Reactor? = null

        private val stopTasks = CopyOnWriteArrayList<() -> Unit>()
        private val shuttingDown = AtomicBoolean(false)

        val loggerFactory: LoggerFactory get() = instance.loggerFactory

        val schedulerProvider: SchedulerProvider get() = instance.schedulerProvider

        val pluginCatalog: PluginCatalog get() = instance.pluginCatalog
        val pluginLifecycleControl: PluginLifecycleControl get() = instance.pluginLifecycleControl
        val pluginScopeFactory: PluginScopeFactory get() = instance.pluginScopeFactory

        fun init(
            loggerFactory: LoggerFactory,
            schedulerProvider: SchedulerProvider,
            pluginCatalog: PluginCatalog,
            pluginLifecycleControl: PluginLifecycleControl,
            pluginScopeFactory: PluginScopeFactory
        ) {
            check(ref == null) { "Kernel already initialized." }

            ref = Reactor(
                loggerFactory, schedulerProvider,
                pluginCatalog, pluginLifecycleControl, pluginScopeFactory
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
