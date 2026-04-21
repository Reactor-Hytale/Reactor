package ink.reactor.plugin.debug

import ink.reactor.kernel.plugin.spi.lifecycle.BasePluginLifecycle
import ink.reactor.sdk.plugin.annotation.Plugin

@Plugin(
    id = "ReactorDebugPlugin", version = "1.0.0-ALPHA"
)
class DebugPlugin: BasePluginLifecycle() {
    override fun enable() {

    }
}
