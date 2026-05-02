package codes.reactor.plugin.discordhook

import codes.reactor.plugin.discordhook.config.DiscordHookConfig
import codes.reactor.plugin.discordhook.webhook.DiscordWebHook
import codes.reactor.kernel.Reactor
import codes.reactor.kernel.plugin.spi.lifecycle.BasePluginLifecycle
import codes.reactor.sdk.plugin.annotation.Plugin

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
