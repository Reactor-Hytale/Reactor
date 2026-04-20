package ink.reactor.sdk.config

import java.io.IOException
import java.nio.file.Path

/**
 * Defines a configuration service capable of loading, saving, and creating configuration files
 * for one or more file extensions.
 */
interface ConfigService {

    /**
     * The list of file extensions supported by this service, without a leading dot.
     *
     * <p>The first extension in the list is considered the primary extension and is used
     * whenever an extension must be inferred automatically.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * override val fileExtensions: List<String> = listOf("yaml", "yml")
     * }</pre>
     */
    val fileExtensions: List<String>

    /**
     * Loads a configuration section from the given path.
     *
     * @param path the path of the configuration file to load
     * @return the loaded configuration section
     * @throws IOException if the file cannot be read or parsed
     */
    @Throws(IOException::class)
    fun load(path: Path): ConfigSection

    /**
     * Saves the given configuration section to the specified path.
     *
     * @param path the destination file path
     * @param section the configuration section to save
     * @param options optional save behavior settings
     * @throws IOException if the file cannot be written
     */
    @Throws(IOException::class)
    fun save(
        path: Path,
        section: ConfigSection,
        options: SaveOptions = SaveOptions()
    )

    /**
     * Creates the file from a bundled resource if it does not exist, then loads it.
     *
     * <p>The provided [fileName] may include or omit the extension:</p>
     *
     * <ul>
     *   <li>{@code "config"} -> resolved to {@code "config.<primaryExtension>"}</li>
     *   <li>{@code "config.yaml"} -> used as-is if supported by this service</li>
     * </ul>
     *
     * <p>The resulting file is created in the current working directory.</p>
     *
     * @param fileName the resource name and output file name, with or without extension
     * @param classLoader the class loader used to resolve the bundled resource
     * @return the loaded configuration section
     * @throws IOException if the file cannot be copied or loaded
     */
    @Throws(IOException::class)
    fun createIfAbsentAndLoad(
        fileName: String,
        classLoader: ClassLoader = Thread.currentThread().contextClassLoader
    ): ConfigSection

    /**
     * Creates the file from a bundled resource if it does not exist, then loads it.
     *
     * <p>The provided [fileName] may include or omit the extension:</p>
     *
     * <ul>
     *   <li>{@code "config"} -> resolved to {@code "config.<primaryExtension>"}</li>
     *   <li>{@code "config.yaml"} -> used as-is if supported by this service</li>
     * </ul>
     *
     * <p>The resulting file is created inside [outputDirectory].</p>
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
     * @param outputDirectory the directory where the file should be created
     * @param classLoader the class loader used to resolve the bundled resource
     * @return the loaded configuration section
     * @throws IOException if the file cannot be copied or loaded
     */
    @Throws(IOException::class)
    fun createIfAbsentAndLoad(
        fileName: String,
        outputDirectory: Path,
        classLoader: ClassLoader = Thread.currentThread().contextClassLoader
    ): ConfigSection

    /**
     * Creates the file from a bundled resource if it does not exist, then loads it.
     *
     * <p>This overload allows the bundled resource name and the output file path to differ.
     * This is useful when a resource should be copied under a custom file name or to a fully
     * explicit file location.</p>
     *
     * <p>Examples:</p>
     * <pre>{@code
     * createIfAbsentAndLoadToFile("config", Path.of("plugins/MyPlugin/custom-config.yaml"))
     * createIfAbsentAndLoadToFile("defaults/main", Path.of("plugins/MyPlugin/config.yaml"))
     * }</pre>
     *
     * @param resourceName the bundled resource name, with or without extension
     * @param outputFile the exact output file path to create if absent
     * @param classLoader the class loader used to resolve the bundled resource
     * @return the loaded configuration section
     * @throws IOException if the file cannot be copied or loaded
     */
    @Throws(IOException::class)
    fun createIfAbsentAndLoadToFile(
        resourceName: String,
        outputFile: Path,
        classLoader: ClassLoader = Thread.currentThread().contextClassLoader
    ): ConfigSection
}
