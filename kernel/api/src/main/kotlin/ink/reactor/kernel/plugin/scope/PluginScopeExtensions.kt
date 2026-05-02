package codes.reactor.kernel.plugin.scope

import codes.reactor.kernel.Reactor
import codes.reactor.kernel.event.EventBus
import codes.reactor.kernel.plugin.spi.lifecycle.PluginLifecycle

fun PluginScope.eventbus(): EventBus =
    this[EventBus::class.java] ?: throw IllegalStateException("EventBus not found in the current PluginScope")

val PluginLifecycle.scope: PluginScope
    get() = Reactor.pluginScopeFactory.acquire()
