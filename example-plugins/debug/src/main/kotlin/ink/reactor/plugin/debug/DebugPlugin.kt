package ink.reactor.plugin.debug

import ink.reactor.kernel.logger.logger
import ink.reactor.kernel.plugin.spi.lifecycle.BasePluginLifecycle
import ink.reactor.sdk.plugin.annotation.Plugin

@Plugin(id = "ReactorDebugPlugin", version = "1.0.0-ALPHA")
class DebugPlugin: BasePluginLifecycle() {

    override fun load() {
        this.logger().info("Loaded!")
    }

    override fun enable() {
        this.logger().info("Enabled!")
        Thread.sleep(1000)
        // TODO: Add config system
    }
}
