package codes.reactor.sdk.extension

import codes.reactor.kernel.Reactor
import codes.reactor.kernel.event.EventBus
import codes.reactor.kernel.plugin.scope.PluginScope
import codes.reactor.kernel.plugin.spi.lifecycle.PluginLifecycle
import codes.reactor.sdk.scanner.PackageScanner
import codes.reactor.sdk.scanner.ScanResult

fun PluginScope.eventbus(): EventBus =
    this[EventBus::class.java] ?: throw IllegalStateException("EventBus not found in the current PluginScope")

fun PluginScope.createScanner(): PackageScanner =
    this[PackageScanner::class.java] ?: throw IllegalStateException("PackageScanner not found in the current PluginScope")

fun PluginScope.scan(
    block: PackageScanner.() -> Unit
): ScanResult {
    val scanner = createScanner()
    scanner.block()
    return scanner.scan()
}

val PluginLifecycle.scope: PluginScope
    get() = Reactor.pluginScopeFactory.acquire()
