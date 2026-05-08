package codes.reactor.plugin.debug

import codes.reactor.sdk.config.ConfigSection
import codes.reactor.sdk.config.ConfigServiceRegistry
import kotlin.time.Duration.Companion.seconds

class DebugConfig(
    section: ConfigSection
) {
    val testString by section.string("default-String")
    val exampleDuration by section.duration(20.seconds)

    companion object {
        fun load(): DebugConfig {
            val section = ConfigServiceRegistry["yaml"].createIfAbsentAndLoad("config")
            return DebugConfig(section)
        }
    }
}
