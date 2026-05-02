package ink.reactor.plugin.discordhook

import ink.reactor.plugin.discordhook.config.DiscordHookConfig
import ink.reactor.plugin.discordhook.webhook.DiscordWebHook
import ink.reactor.kernel.Reactor
import ink.reactor.kernel.plugin.spi.lifecycle.BasePluginLifecycle
import ink.reactor.sdk.plugin.annotation.Plugin

@Plugin(id = "discord-hook", version = "1.0.0")
class DiscordHookPlugin: BasePluginLifecycle() {

    override fun enable() {
        DiscordWebHook.init()
        val config = DiscordHookConfig.load()
        Reactor.loggerSpy.register(DiscordLogger(config))
    }

    override fun disable() {
        DiscordWebHook.shutdown()
    }
}
