package ink.reactor.sdk.bundled.config.yaml

import ink.reactor.sdk.bundled.config.AbstractConfigService
import ink.reactor.sdk.config.ConfigSection
import ink.reactor.sdk.config.section.MapConfigSection
import ink.reactor.sdk.config.SaveOptions
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.nio.file.Files
import java.nio.file.Path

class YamlConfigService : AbstractConfigService() {
    override val fileExtensions = listOf("yml", "yaml")

    private val loader: Load by lazy {
        val settings = LoadSettings.builder()
            .setParseComments(false)
            .setDefaultMap { size -> LinkedHashMap<Any, Any>(size) }
            .build()
        Load(settings, YamlCustomConstructor(settings))
    }

    override fun load(path: Path): ConfigSection {
        return Files.newBufferedReader(path).use { reader ->
            val result = loader.loadFromReader(reader)
            (result as? ConfigSection) ?: MapConfigSection()
        }
    }

    override fun save(
        path: Path,
        section: ConfigSection,
        options: SaveOptions
    ) {
        Files.newBufferedWriter(path).use { writer ->
            writer.write(SectionToYamlConverter(options.indentSpaces).toYaml(section));
        }
    }
}
