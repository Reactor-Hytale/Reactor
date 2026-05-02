package ink.reactor.microkernel

import ink.reactor.kernel.Reactor
import ink.reactor.kernel.event.EventBus
import ink.reactor.kernel.logger.Logger
import ink.reactor.kernel.scheduler.SchedulerProvider
import ink.reactor.microkernel.config.KernelPluginConfig
import ink.reactor.microkernel.event.bus.DefaultEventBus
import ink.reactor.microkernel.logger.MicrokernelLoggerFactory
import ink.reactor.microkernel.plugin.catalog.DefaultPluginCatalog
import ink.reactor.microkernel.plugin.lifecycle.DefaultPluginLifecycleControl
import ink.reactor.microkernel.plugin.PluginInstaller
import ink.reactor.microkernel.plugin.PluginStarter
import ink.reactor.microkernel.plugin.library.LibraryPathResolver
import ink.reactor.microkernel.plugin.library.PluginLibraryResolver
import ink.reactor.microkernel.plugin.manifest.PluginPropertiesReader
import ink.reactor.microkernel.plugin.scanner.PluginScanner
import ink.reactor.microkernel.plugin.scope.KernelPluginScopeFactory
import ink.reactor.microkernel.plugin.validation.PluginDependencyValidator
import ink.reactor.microkernel.scheduler.KernelSchedulerProvider

class Microkernel private constructor(
    val pluginConfig: KernelPluginConfig,
    val rootBus: EventBus,
    val rootLogger: Logger
) {

    companion object {
        @Volatile
        private var ref: Microkernel? = null

        fun init(
            logger: Logger,
            kernelPluginConfig: KernelPluginConfig,
            eventBus: EventBus = DefaultEventBus(logger),
            schedulerProvider: SchedulerProvider = KernelSchedulerProvider(),
            parentClassLoader: ClassLoader = Reactor::class.java.classLoader
        ) {
            check(ref == null) { "Microkernel already initialized." }

            val pluginCatalog = DefaultPluginCatalog()
            val libraryResolver = PluginLibraryResolver(
                LibraryPathResolver(kernelPluginConfig.paths.libraries)
            )

            val pluginLifecycleControl = DefaultPluginLifecycleControl(
                pluginCatalog, libraryResolver, kernelPluginConfig, logger, parentClassLoader
            )

            Reactor.init(
                MicrokernelLoggerFactory(logger),
                schedulerProvider,
                pluginCatalog, pluginLifecycleControl,
                KernelPluginScopeFactory(),
                kernelPluginConfig.paths.plugins,
                kernelPluginConfig.paths.libraries
            )

            ref = Microkernel(kernelPluginConfig, eventBus, logger)

            Reactor.addStopTask {
                pluginLifecycleControl.shutdown()
            }

            val scanner = PluginScanner(PluginPropertiesReader(), logger)
            val installer = PluginInstaller(
                kernelPluginConfig, pluginCatalog,
                scanner, PluginDependencyValidator(), logger
            )
            val selectedPluginIds = installer.install(
                kernelPluginConfig.paths.plugins.toFile()
            )

            PluginStarter(
                kernelPluginConfig, pluginCatalog, pluginLifecycleControl,
                logger
            ).start(selectedPluginIds)
        }

        val instance: Microkernel
            get() = ref ?: error("Microkernel not initialized.")
    }
}
