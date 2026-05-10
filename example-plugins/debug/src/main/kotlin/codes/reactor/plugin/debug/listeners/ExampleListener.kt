package codes.reactor.plugin.debug.listeners

import codes.reactor.kernel.event.Listener
import codes.reactor.kernel.event.handler.ListenerPhase
import codes.reactor.kernel.logger.logger
import codes.reactor.kernel.plugin.spi.lifecycle.PluginLifecycle

// Used to test PackageScanner and ListenerRegistrar
class ExampleListener(
    private val plugin: PluginLifecycle
) {

    @Listener(phase = ListenerPhase.MONITOR, priority = 255, ignoreCancelled = true)
    fun exampleListener(test: String) {
        plugin.logger().info("ExampleListener $test")
    }

    @Listener(priority = 1)
    fun exampleListener2(test: String) {
        plugin.logger().info("ExampleListener2 $test")
    }

    @Listener
    fun exampleListener3(test: String) {
        plugin.logger().info("ExampleListener3 $test")
    }
}
