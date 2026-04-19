package ink.reactor.launcher.logger

import ink.reactor.sdk.config.ConfigSection
import ink.reactor.sdk.util.TimeFormatter

class LoggerConfig(section: ConfigSection) {
    val enable by section.delegate(true)
    val prefix = PrefixConfig(section.getOrCreateSection("prefix"))
    val console = ConsoleConfig(section.getOrCreateSection("console"))
    val logs = FileLogsConfig(section.getOrCreateSection("logs"))
}

class PrefixConfig(section: ConfigSection) {
    val dateFormatter by section.delegate("HH:mm:ss")

    val debug by section.delegate("[DEBUG %time%] ")
    val log   by section.delegate("[LOG %time%] ")
    val info  by section.delegate("[INFO %time%] ")
    val warn  by section.delegate("[WARN %time%] ")
    val error by section.delegate("[ERROR %time%] ")
}

class ConsoleConfig(section: ConfigSection) {
    val enable by section.delegate(true)
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
    val prefix by section.delegate("")
    val text by section.delegate("")
    val afterText by section.delegate("")
}

class FileLogsConfig(section: ConfigSection) {
    val enable      by section.delegate(true)
    val logsFolder  by section.delegate("logs")
    val bufferSize  by section.delegate(8192)

    val maxFileSize by section.delegate(5_000_000L)
    val compression by section.delegate(true)

    val levels      = LoggerLevels(section.getOrCreateSection("levels"))
    val autoFlush   = AutoFlushConfig(section.getOrCreateSection("auto-flush"))
}

class AutoFlushConfig(section: ConfigSection) {
    val enable by section.delegate(true)
    val interval = TimeFormatter.parseToSeconds(section.getString("interval") ?: "10s")
}

class LoggerLevels(section: ConfigSection) {
    val debug by section.delegate(false)
    val log   by section.delegate(true)
    val info  by section.delegate(true)
    val warn  by section.delegate(true)
    val error by section.delegate(true)
}
