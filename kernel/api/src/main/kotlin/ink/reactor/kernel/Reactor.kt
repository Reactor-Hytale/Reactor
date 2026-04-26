package ink.reactor.kernel

import ink.reactor.kernel.event.EventBus
import ink.reactor.kernel.logger.Logger
import ink.reactor.kernel.logger.LoggerFactory/*
import ink.reactor.kernel.plugin.control.PluginLifecycleControl
import ink.reactor.kernel.plugin.query.PluginCatalog
import ink.reactor.kernel.scheduler.SchedulerProvider*/
import ink.reactor.kernel.plugin.control.PluginLifecycleControl
import ink.reactor.kernel.plugin.query.PluginCatalog
import ink.reactor.kernel.scheduler.SchedulerProvider
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class Reactor private constructor(
    val kernelLogger: Logger,
    val loggerFactory: LoggerFactory,

    val bus: EventBus,
    val schedulerProvider: SchedulerProvider,
    val pluginCatalog: PluginCatalog,
    val pluginLifecycleControl: PluginLifecycleControl
) {

    companion object {
        @Volatile
        private var ref: Reactor? = null

        private val stopTasks = CopyOnWriteArrayList<() -> Unit>()
        private val shuttingDown = AtomicBoolean(false)

        val globalLogger: Logger get() = instance.kernelLogger
        val loggerFactory: LoggerFactory get() = instance.loggerFactory

        val bus: EventBus get() = instance.bus
        val schedulerProvider: SchedulerProvider get() = instance.schedulerProvider

        val pluginCatalog: PluginCatalog get() = instance.pluginCatalog
        val pluginLifecycleControl: PluginLifecycleControl get() = instance.pluginLifecycleControl

        fun init(
            logger: Logger,
            loggerFactory: LoggerFactory,
            bus: EventBus,
            schedulerProvider: SchedulerProvider,
            pluginCatalog: PluginCatalog,
            pluginLifecycleControl: PluginLifecycleControl,
        ) {
            check(ref == null) { "Kernel already initialized." }

            ref = Reactor(
                logger, loggerFactory, bus, schedulerProvider,
                pluginCatalog, pluginLifecycleControl
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
