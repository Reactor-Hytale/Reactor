package ink.reactor.plugin.discordhook.config

import ink.reactor.sdk.config.ConfigSection
import ink.reactor.sdk.config.ConfigServiceRegistry
import java.time.format.DateTimeFormatter
import java.util.Objects
import kotlin.Any

class DiscordHookConfig(
    section: ConfigSection
) {
    val debug = LevelSection(section.getOrCreateSection("debug"))
    val log = LevelSection(section.getOrCreateSection("log"))
    val info = LevelSection(section.getOrCreateSection("info"))
    val warn = LevelSection(section.getOrCreateSection("warn"))
    val error = LevelSection(section.getOrCreateSection("error"))

    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
        section.getOrDefault("date-format", "HH:mm:ss"))

    companion object {
        fun load(): DiscordHookConfig {
            val section = ConfigServiceRegistry["yaml"].createIfAbsentAndLoad("config")
            return DiscordHookConfig(section)
        }
    }
}

class LevelSection(section: ConfigSection) {
    val enable by section.boolean(false)
    val webhookUrl by section.string("")
    val title by section.string(section.name)
    val description = objectToString(section["description"])
}

private fun objectToString(any: Any?): String {
    if (any is String) {
        return any
    }
    if (any is MutableList<*>) {
        return java.lang.String.join(
            "\n",
            any.stream().map { `object`: Any? -> objectToString(`object`) }.toList()
        )
    }
    return Objects.toString(any)
}
