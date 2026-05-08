package codes.reactor.launcher.plugin

import codes.reactor.kernel.plugin.model.PluginId
import codes.reactor.kernel.plugin.model.version.Version
import codes.reactor.microkernel.config.KernelPluginConfig
import codes.reactor.sdk.config.ConfigService
import codes.reactor.sdk.config.ConfigSection
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class PluginConfigLoader {

    fun load(configService: ConfigService): KernelPluginConfig {
        val config = configService.createIfAbsentAndLoad("plugins")

        val pathsSection = config.getOrCreateSection("paths")
        val selectionSection = config.getOrCreateSection("selection")
        val loadingSection = config.getOrCreateSection("loading")
        val loggingSection = config.getOrCreateSection("logging")
        val startupBufferSection = loggingSection.getOrCreateSection("startup-buffer")

        return KernelPluginConfig(
            loadPaths(pathsSection),
            loadSelection(selectionSection),
            loadLoadingSection(loadingSection),
            loadLogging(loggingSection, startupBufferSection)
        )
    }

    private fun loadPaths(section: ConfigSection): KernelPluginConfig.Paths {
        return KernelPluginConfig.Paths(
            Path.of(section.getOrDefault("plugins", "/plugins")),
                Path.of(section.getOrDefault("libraries", "/cache/libs"))
        )
    }

    private fun loadSelection(section: ConfigSection): KernelPluginConfig.Selection {

        val plugins = section.getStringList("plugins")
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map(::parsePluginFormat)
            .toList()

        val duplicates = plugins.groupBy { it.id }
            .filterValues { it.size > 1 }

        require(duplicates.isEmpty()) {
            "Duplicate plugin entries: ${duplicates.keys.joinToString()}"
        }

        return KernelPluginConfig.Selection(
            parseEnumOrDefault(
                section.getString("mode"),
                KernelPluginConfig.Selection.Mode.ALL
            ),
            plugins
        )
    }

    private fun loadLoadingSection(section: ConfigSection): KernelPluginConfig.Loading {
        val timeOut = Duration.parseOrNull(section.getOrDefault("timeout", "5s")) ?: 5.seconds
        return KernelPluginConfig.Loading(
            section.getBoolean("parallel", true),
            timeOut
        )
    }

    private fun loadLogging(
        loggingSection: ConfigSection,
        startupBufferSection: ConfigSection
    ): KernelPluginConfig.Logging {
        return KernelPluginConfig.Logging(
            loggingSection.getBoolean("lifecycle-events", true),
            startupBufferSection.getBoolean("enabled", true),
            startupBufferSection
                .getInt("initial-capacity", 64)
                .coerceAtLeast(1),
            startupBufferSection
                .getInt("max-lines", 1000)
                .coerceAtLeast(1),
            parseEnumOrDefault(
                startupBufferSection.getString("overflow-strategy"),
                KernelPluginConfig.Logging.Strategy.DROP_NEWEST
            )
        )
    }

    private fun parsePluginFormat(raw: String): KernelPluginConfig.Selection.PluginFormat {
        val normalized = raw.trim()

        require(normalized.isNotEmpty()) {
            "Plugin selection entry cannot be blank."
        }

        val separatorIndex = normalized.indexOf(':')

        if (separatorIndex < 0) {
            return KernelPluginConfig.Selection.PluginFormat(
                id = PluginId(normalized),
                version = null
            )
        }

        val id = PluginId(normalized.substring(0, separatorIndex).trim())
        val versionRaw = normalized.substring(separatorIndex + 1).trim()

        require(versionRaw.isNotEmpty()) {
            "Invalid plugin selection entry '$raw': version cannot be blank after ':'."
        }

        return KernelPluginConfig.Selection.PluginFormat(id, Version.parse(versionRaw))
    }

    private inline fun <reified E : Enum<E>> parseEnumOrDefault(
        raw: String?,
        default: E
    ): E {
        if (raw.isNullOrBlank()) {
            return default
        }

        val normalized = raw
            .trim()
            .uppercase()
            .replace('-', '_')
            .replace(' ', '_')

        return try {
            enumValueOf<E>(normalized)
        } catch (_: IllegalArgumentException) {
            default
        }
    }
}
