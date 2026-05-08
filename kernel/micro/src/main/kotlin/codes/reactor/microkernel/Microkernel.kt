package codes.reactor.microkernel

import codes.reactor.kernel.Reactor
import codes.reactor.kernel.event.EventBus
import codes.reactor.kernel.logger.Logger
import codes.reactor.kernel.scheduler.SchedulerProvider
import codes.reactor.microkernel.config.KernelPluginConfig
import codes.reactor.microkernel.event.bus.DefaultEventBus
import codes.reactor.microkernel.logger.MicrokernelLoggerFactory
import codes.reactor.microkernel.plugin.catalog.DefaultPluginCatalog
import codes.reactor.microkernel.plugin.lifecycle.DefaultPluginLifecycleControl
import codes.reactor.microkernel.plugin.PluginInstaller
import codes.reactor.microkernel.plugin.PluginStarter
import codes.reactor.microkernel.plugin.library.LibraryPathResolver
import codes.reactor.microkernel.plugin.library.PluginLibraryResolver
import codes.reactor.microkernel.plugin.manifest.PluginPropertiesReader
import codes.reactor.microkernel.plugin.scanner.PluginScanner
import codes.reactor.microkernel.plugin.scope.KernelPluginScopeFactory
import codes.reactor.microkernel.plugin.scope.extension.KernelLoggerSpyScope
import codes.reactor.microkernel.plugin.validation.PluginDependencyValidator
import codes.reactor.microkernel.scheduler.KernelSchedulerProvider

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

            val pluginScopeFactory = KernelPluginScopeFactory()
            Reactor.init(
                MicrokernelLoggerFactory(logger), KernelLoggerSpyScope(pluginScopeFactory),
                schedulerProvider,
                pluginCatalog, pluginLifecycleControl,
                pluginScopeFactory,
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
