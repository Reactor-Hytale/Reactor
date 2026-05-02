package codes.reactor.launcher

import codes.reactor.launcher.console.Console
import codes.reactor.launcher.console.JLineConsole.createConsole
import codes.reactor.launcher.logger.LoggersLoader
import codes.reactor.launcher.network.NetworkLoader
import codes.reactor.launcher.plugin.PluginConfigLoader
import codes.reactor.microkernel.Microkernel
import codes.reactor.sdk.bundled.config.yaml.YamlConfigService
import codes.reactor.sdk.config.ConfigServiceRegistry

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
