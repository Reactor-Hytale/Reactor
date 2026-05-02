package codes.reactor.sdk.bundled.config

import codes.reactor.kernel.Reactor
import codes.reactor.sdk.config.ConfigSection
import codes.reactor.sdk.config.ConfigService
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

/**
 * Base implementation of [ConfigService] for services that load default configuration files
 * from bundled classpath resources.
 *
 * <p>This abstract class provides the common logic for:</p>
 *
 * <ul>
 *   <li>normalizing file names with missing extensions</li>
 *   <li>copying default resources from the classpath if the output file does not exist</li>
 *   <li>loading the resulting output file</li>
 * </ul>
 *
 * <p>The first entry in [fileExtensions] is treated as the primary extension and is used
 * whenever the caller omits the extension.</p>
 */
abstract class AbstractConfigService : ConfigService {

    /**
     * The primary file extension used by this service.
     *
     * <p>This is always the first normalized value from [fileExtensions].</p>
     */
    protected val primaryExtension: String
        get() = fileExtensions.first().normalizedExtension()

    /**
     * Creates the file in the current working directory if absent, then loads it.
     *
     * <p>If [fileName] has no supported extension, the primary extension is appended.</p>
     *
     * @param fileName the resource name and output file name, with or without extension
     * @param classLoader the class loader used to resolve the bundled resource
     * @return the loaded configuration section
     * @throws IOException if the file cannot be copied or loaded
     */
    @Throws(IOException::class)
    override fun createIfAbsentAndLoad(
        fileName: String,
        classLoader: ClassLoader
    ): ConfigSection {
        val normalizedFileName = normalizeFileName(fileName)
        val outputFile = Path.of(normalizedFileName)
        return createIfAbsentAndLoadToFile(normalizedFileName, outputFile, classLoader)
    }

    /**
     * Creates the file inside the provided directory if absent, then loads it.
     *
     * <p>If [fileName] has no supported extension, the primary extension is appended.</p>
     *
     * <p>Examples:</p>
     * <pre>{@code
     * createIfAbsentAndLoad("config", Path.of("plugins/MyPlugin"))
     * // -> plugins/MyPlugin/config.yaml
     *
     * createIfAbsentAndLoad("config.yaml", Path.of("plugins/MyPlugin"))
     * // -> plugins/MyPlugin/config.yaml
     * }</pre>
     *
     * @param fileName the resource name and output file name, with or without extension
     * @param outputDirectory the directory where the output file should be created
     * @param classLoader the class loader used to resolve the bundled resource
     * @return the loaded configuration section
     * @throws IOException if the file cannot be copied or loaded
     */
    @Throws(IOException::class)
    override fun createIfAbsentAndLoad(
        fileName: String,
        outputDirectory: Path,
        classLoader: ClassLoader
    ): ConfigSection {
        val normalizedFileName = normalizeFileName(fileName)
        val outputFile = outputDirectory.resolve(normalizedFileName)
        return createIfAbsentAndLoadToFile(normalizedFileName, outputFile, classLoader)
    }

    /**
     * Creates the output file from a bundled resource if it does not already exist,
     * then loads it.
     *
     * <p>Both [resourceName] and [outputFile] are resolved independently:</p>
     *
     * <ul>
     *   <li>[resourceName] may omit the extension and will be normalized</li>
     *   <li>[outputFile] is treated as the exact destination file path</li>
     * </ul>
     *
     * <p>If the output file already exists, no copy occurs and the existing file is loaded.</p>
     *
     * @param resourceName the bundled resource name, with or without extension
     * @param outputFile the exact file path to create if absent
     * @param classLoader the class loader used to resolve the bundled resource
     * @return the loaded configuration section
     * @throws IOException if the file cannot be copied or loaded
     * @throws FileNotFoundException if the bundled resource cannot be found
     */
    @Throws(IOException::class)
    override fun createIfAbsentAndLoadToFile(
        resourceName: String,
        outputFile: Path,
        classLoader: ClassLoader
    ): ConfigSection {
        var outputFile = outputFile
        val normalizedResourceName = normalizeFileName(resourceName)

        runCatching { // This code can be run from the launcher, even if the kernel hasn't been loaded yet
            val snapshot = Reactor.pluginCatalog.getFromCurrentScope()
            if (snapshot != null) {
                outputFile = Reactor.pluginDirectory.resolve(snapshot.metadata.id.value).resolve(outputFile)
            }
        }

        if (outputFile.exists()) {
            return load(outputFile)
        }

        val input = classLoader.getResourceAsStream(normalizedResourceName)
            ?: throw FileNotFoundException(
                "Cannot find bundled config resource '$normalizedResourceName' in the classpath."
            )

        input.use {
            outputFile.parent?.createDirectories()
            Files.copy(it, outputFile, StandardCopyOption.REPLACE_EXISTING)
        }

        return load(outputFile)
    }

    /**
     * Normalizes the given file name by ensuring it ends with one of the supported extensions.
     *
     * <p>If the file name already ends with a supported extension, it is returned unchanged.
     * Otherwise, the primary extension is appended.</p>
     *
     * <p>Examples for a YAML service:</p>
     * <pre>{@code
     * normalizeFileName("config")      -> "config.yaml"
     * normalizeFileName("config.yaml") -> "config.yaml"
     * normalizeFileName("config.yml")  -> "config.yml"
     * }</pre>
     *
     * @param fileName the file name to normalize
     * @return the normalized file name
     * @throws IllegalArgumentException if the file name is blank
     */
    protected fun normalizeFileName(fileName: String): String {
        require(fileName.isNotBlank()) {
            "fileName cannot be blank."
        }

        val lowercaseName = fileName.lowercase()
        val hasSupportedExtension = fileExtensions.any { extension ->
            lowercaseName.endsWith(".${extension.normalizedExtension()}")
        }

        return if (hasSupportedExtension) {
            fileName
        } else {
            "$fileName.$primaryExtension"
        }
    }

    /**
     * Normalizes a file extension by removing a leading dot and converting it to lowercase.
     *
     * @return the normalized extension
     */
    private fun String.normalizedExtension(): String {
        return trim().removePrefix(".").lowercase()
    }
}
