package ink.reactor.launcher

import ink.reactor.kernel.Reactor
import ink.reactor.launcher.console.Console
import ink.reactor.launcher.console.JLineConsole.createConsole
import ink.reactor.launcher.logger.LoggersLoader
import ink.reactor.launcher.network.NetworkLoader
import ink.reactor.microkernel.event.simplebus.SimpleEventBus
import ink.reactor.microkernel.logger.SimpleLoggerFactory
import ink.reactor.microkernel.scheduler.KernelSchedulerProvider
import ink.reactor.sdk.bundled.config.yaml.YamlConfigService
import ink.reactor.sdk.config.ConfigServiceRegistry

fun start(args: Array<String>) {
    MinimalReactorLauncher.start()
}

class MinimalReactorLauncher internal constructor() {

    companion object {
        @JvmStatic
        fun start() {
            MinimalReactorLauncher().startServer()?.run()
        }
    }

    internal fun startServer(): Console? {
        val startTime = System.currentTimeMillis()

        val console = createConsole() ?: return null

        val yamlConfigService = YamlConfigService()
        ConfigServiceRegistry.register(yamlConfigService)

        val logger = LoggersLoader(console.terminal.writer()).load(yamlConfigService)

        Reactor.init(
            logger,
            SimpleLoggerFactory(logger),
            SimpleEventBus(logger),
            KernelSchedulerProvider()
        )

        NetworkLoader(logger).load(yamlConfigService)

        logger.info("Server started in ${System.currentTimeMillis() - startTime}ms")
        return console
    }
}
