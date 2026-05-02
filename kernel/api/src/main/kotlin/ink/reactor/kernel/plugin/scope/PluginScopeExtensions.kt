package ink.reactor.kernel.plugin.scope

import ink.reactor.kernel.Reactor
import ink.reactor.kernel.event.EventBus
import ink.reactor.kernel.plugin.spi.lifecycle.PluginLifecycle

fun PluginScope.eventbus(): EventBus =
    this[EventBus::class.java] ?: throw IllegalStateException("EventBus not found in the current PluginScope")

val PluginLifecycle.scope: PluginScope
    get() = Reactor.pluginScopeFactory.acquire()
