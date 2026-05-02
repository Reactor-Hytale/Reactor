package codes.reactor.plugin.debug

import codes.reactor.kernel.event.handler.ListenerPhase
import codes.reactor.kernel.event.subscribe
import codes.reactor.kernel.logger.logger
import codes.reactor.kernel.plugin.scope.eventbus
import codes.reactor.kernel.plugin.scope.scope
import codes.reactor.kernel.plugin.spi.lifecycle.BasePluginLifecycle
import codes.reactor.sdk.plugin.annotation.Plugin

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
