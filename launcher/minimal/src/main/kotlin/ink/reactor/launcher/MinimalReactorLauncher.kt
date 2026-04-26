package ink.reactor.launcher

import ink.reactor.launcher.console.Console
import ink.reactor.launcher.console.JLineConsole.createConsole
import ink.reactor.launcher.logger.LoggersLoader
import ink.reactor.launcher.network.NetworkLoader
import ink.reactor.launcher.plugin.PluginConfigLoader
import ink.reactor.microkernel.Microkernel
import ink.reactor.sdk.bundled.config.yaml.YamlConfigService
import ink.reactor.sdk.config.ConfigServiceRegistry

fun start(publicClassLoader: ClassLoader) {
    MinimalReactorLauncher.start(publicClassLoader)
}

class MinimalReactorLauncher internal constructor() {

    companion object {
        @JvmStatic
        fun start(publicClassLoader: ClassLoader) {
            MinimalReactorLauncher().startServer(publicClassLoader)?.run()
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
