package ink.reactor.plugin.debug

import ink.reactor.kernel.event.handler.ListenerPhase
import ink.reactor.kernel.event.subscribe
import ink.reactor.kernel.logger.logger
import ink.reactor.kernel.plugin.scope.eventbus
import ink.reactor.kernel.plugin.scope.scope
import ink.reactor.kernel.plugin.spi.lifecycle.BasePluginLifecycle
import ink.reactor.sdk.plugin.annotation.Plugin

@Plugin(id = "ReactorDebugPlugin", version = "1.0.0-ALPHA")
class DebugPlugin: BasePluginLifecycle() {
    private val logger = this.logger()

    override fun load() {
        logger.info("Loaded!")
    }

    override fun enable() {
        logger.info("Enabled!")

        // You don't need to unsubscribe events using scope()
        val eventbus = scope.eventbus()

        eventbus.subscribe<String>(
            phase = ListenerPhase.MONITOR,
            priority = 255
        ) {
            logger.info("Received $it")
        }

        val config = DebugConfig.load()
        logger.info("Example config: test-string: ${config.testString} - duration: ${config.exampleDuration}")
    }
}
