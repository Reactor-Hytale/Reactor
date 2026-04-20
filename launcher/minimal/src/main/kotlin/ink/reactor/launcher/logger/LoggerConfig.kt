package ink.reactor.launcher.logger

import ink.reactor.sdk.config.ConfigSection
import ink.reactor.sdk.util.TimeFormatter
import kotlin.time.Duration.Companion.seconds

class LoggerConfig(section: ConfigSection) {
    val enable by section.boolean(true)

    val prefix = PrefixConfig(section.getOrCreateSection("prefix"))
    val console = ConsoleConfig(section.getOrCreateSection("console"))
    val logs = FileLogsConfig(section.getOrCreateSection("logs"))
}

class PrefixConfig(section: ConfigSection) {
    val dateFormatter by section.string("HH:mm:ss")

    val debug by section.string("[DEBUG %time%] ")
    val log   by section.string("[LOG %time%] ")
    val info  by section.string("[INFO %time%] ")
    val warn  by section.string("[WARN %time%] ")
    val error by section.string("[ERROR %time%] ")
}

class ConsoleConfig(section: ConfigSection) {
    val enable by section.boolean(true)
    val levels = LoggerLevels(section.getOrCreateSection("levels"))
    val styles = StylesConfig(section.getOrCreateSection("styles"))
}

class StylesConfig(section: ConfigSection) {
    val debug = ConsoleStyle(section.getOrCreateSection("debug"))
    val log   = ConsoleStyle(section.getOrCreateSection("log"))
    val info  = ConsoleStyle(section.getOrCreateSection("info"))
    val warn  = ConsoleStyle(section.getOrCreateSection("warn"))
    val error = ConsoleStyle(section.getOrCreateSection("error"))
}

class ConsoleStyle(section: ConfigSection) {
    val prefix by section.string("")
    val text by section.string("")
    val afterText by section.string("")
}

class FileLogsConfig(section: ConfigSection) {
    val enable by section.boolean(true)
    val logsFolder by section.string("logs")

    val bufferSize by section.unsignedInt(8192)
    val maxFileSize by section.unsignedLong(5_000_000L)

    val compression by section.boolean(true)

    val levels = LoggerLevels(section.getOrCreateSection("levels"))
    val autoFlush = AutoFlushConfig(section.getOrCreateSection("auto-flush"))
}

class AutoFlushConfig(section: ConfigSection) {
    val interval by section.duration(10.seconds)
}

class LoggerLevels(section: ConfigSection) {
    val debug by section.boolean(false)
    val log   by section.boolean(true)
    val info  by section.boolean(true)
    val warn  by section.boolean(true)
    val error by section.boolean(true)
}
