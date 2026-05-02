package codes.reactor.plugin.discordhook

import codes.reactor.plugin.discordhook.config.DiscordHookConfig
import codes.reactor.plugin.discordhook.config.LevelSection
import codes.reactor.plugin.discordhook.webhook.DiscordMessage
import codes.reactor.plugin.discordhook.webhook.DiscordWebHook
import codes.reactor.kernel.logger.Logger
import codes.reactor.sdk.logger.DummyLoggerFormatter
import codes.reactor.sdk.util.PlaceholderReplacement
import java.time.LocalDateTime

class DiscordLogger(
    private val config: DiscordHookConfig
): Logger {
    override val loggerFormatter = DummyLoggerFormatter

    private fun infoLevel(level: LevelSection, message: String) {
        if (level.enable) {
            DiscordWebHook.sendWebhook(createMessage(message, level.title, level.description, level.webhookUrl))
        }
    }

    override fun debug(message: String) {
        infoLevel(config.debug, message)
    }

    override fun log(message: String) {
        infoLevel(config.log, message)
    }

    override fun info(message: String) {
        infoLevel(config.info, message)
    }

    override fun warn(message: String) {
        infoLevel(config.warn, message)
    }

    override fun error(message: String) {
        infoLevel(config.error, message)
    }

    override fun error(message: String, throwable: Throwable) {
        infoLevel(config.debug, message)
    }

    private fun createMessage(message: String, title: String, description: String, url: String): DiscordMessage {
        val placeholders = mapOf(
            "message" to message,
            "time" to config.dateFormatter.format(LocalDateTime.now()),
            "url" to url
        )
        return DiscordMessage(
            PlaceholderReplacement.apply(title, placeholders),
            PlaceholderReplacement.apply(description, placeholders),
            url
        )
    }
}
