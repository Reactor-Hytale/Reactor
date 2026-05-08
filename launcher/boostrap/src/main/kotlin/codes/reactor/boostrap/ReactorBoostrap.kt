package codes.reactor.boostrap

import codes.reactor.boostrap.console.Console
import codes.reactor.boostrap.console.JLineConsole.createConsole
import codes.reactor.boostrap.logger.LoggersLoader
import codes.reactor.boostrap.network.NetworkLoader
import codes.reactor.boostrap.plugin.PluginConfigLoader
import codes.reactor.microkernel.Microkernel
import codes.reactor.sdk.bundled.config.yaml.YamlConfigService
import codes.reactor.sdk.config.ConfigServiceRegistry

fun start(publicClassLoader: ClassLoader) {
    ReactorBoostrap.start(publicClassLoader)
}

class ReactorBoostrap internal constructor() {

    companion object {
        @JvmStatic
        fun start(publicClassLoader: ClassLoader) {
            ReactorBoostrap().startServer(publicClassLoader)?.run()
        }
    }

    internal fun startServer(publicClassLoader: ClassLoader): Console? {
        val startTime = System.currentTimeMillis()

        val console = createConsole() ?: return null

        val yamlConfigService = YamlConfigService()
        ConfigServiceRegistry.register(yamlConfigService)

        val logger = LoggersLoader(console.terminal.writer()).load(yamlConfigService)

        Microkernel.init(
            logger = logger,
            kernelPluginConfig = PluginConfigLoader().load(yamlConfigService),
            parentClassLoader = publicClassLoader
        )

        NetworkLoader(logger).load(yamlConfigService)

        logger.info("Server started in ${System.currentTimeMillis() - startTime}ms")
        return console
    }
}
