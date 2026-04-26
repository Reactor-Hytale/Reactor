package ink.reactor.microkernel.plugin.lifecycle.runtime

import ink.reactor.kernel.logger.Logger
import ink.reactor.kernel.plugin.exception.PluginLoadException
import ink.reactor.kernel.plugin.library.LibrariesRequest
import ink.reactor.kernel.plugin.model.PluginId
import ink.reactor.kernel.plugin.model.dependency.DependencyKind
import ink.reactor.kernel.plugin.spi.lifecycle.PluginBoostrap
import ink.reactor.kernel.plugin.spi.lifecycle.PluginLifecycle
import ink.reactor.microkernel.plugin.catalog.DefaultPluginCatalog
import ink.reactor.microkernel.plugin.catalog.PluginEntry
import ink.reactor.microkernel.plugin.classloading.PluginClassLoader
import ink.reactor.microkernel.plugin.library.PluginLibraryResolver
import java.net.URL

internal class PluginInstanceCreator(
    private val catalog: DefaultPluginCatalog,
    private val libraryResolver: PluginLibraryResolver,
    private val logger: Logger,
    private val parentClassLoader: ClassLoader
) {
    fun create(entry: PluginEntry): LoadedPlugin {
        val manifest = entry.candidate.manifest
        val dependencyClassLoaders = directClassLoaderParentsOf(entry)
        val pluginUrl = entry.candidate.jarFile.toURI().toURL()
        val librariesRequest = LibrariesRequest()

        runBootstrap(entry, pluginUrl, dependencyClassLoaders, librariesRequest)

        val libraryUrls = try {
            libraryResolver.resolve(librariesRequest)
        } catch (error: Throwable) {
            throw PluginLoadException("Could not resolve libraries for plugin ${entry.id}.", error)
        }

        val runtimeClassLoader = PluginClassLoader(
            entry.id,
            (listOf(pluginUrl) + libraryUrls).toTypedArray(),
            dependencyClassLoaders,
            parentClassLoader
        )

        return try {
            val lifecycle = withPluginClassLoader(runtimeClassLoader) {
                instantiate(runtimeClassLoader, manifest.mainClass, PluginLifecycle::class.java)
            }

            LoadedPlugin(runtimeClassLoader, lifecycle)
        } catch (error: Throwable) {
            runCatching { runtimeClassLoader.close() }
                .onFailure { closeError -> logger.error("Could not close runtime classloader for plugin ${entry.id}.", closeError) }
            throw PluginLoadException("Could not create lifecycle for plugin ${entry.id}.", error)
        }
    }

    private fun runBootstrap(
        entry: PluginEntry,
        pluginUrl: URL,
        dependencyClassLoaders: List<ClassLoader>,
        librariesRequest: LibrariesRequest
    ) {
        val bootstrapClassName = entry.candidate.manifest.boostrapClass ?: return

        val bootstrapClassLoader = PluginClassLoader(
            entry.id,
            arrayOf(pluginUrl),
            dependencyClassLoaders,
            parentClassLoader
        )

        try {
            withPluginClassLoader(bootstrapClassLoader) {
                val bootstrap = instantiate(bootstrapClassLoader, bootstrapClassName, PluginBoostrap::class.java)
                bootstrap.boot(librariesRequest)
            }
        } catch (error: Throwable) {
            throw PluginLoadException("Could not run bootstrap for plugin ${entry.id}.", error)
        } finally {
            runCatching { bootstrapClassLoader.close() }
                .onFailure { error -> logger.error("Could not close bootstrap classloader for plugin ${entry.id}.", error) }
        }
    }

    private fun directClassLoaderParentsOf(entry: PluginEntry): List<ClassLoader> {
        val registeredPluginIds = catalog.entries().mapTo(linkedSetOf()) { it.id }
        val dependencyIds = entry.metadata.dependencies
            .filter { it.kind == DependencyKind.REQUIRED || it.id in registeredPluginIds }
            .map { it.id }
            .distinct()

        return dependencyIds.mapNotNull { dependencyId: PluginId ->
            catalog.entry(dependencyId)?.classLoader
        }
    }

    private fun <T : Any> instantiate(
        classLoader: ClassLoader,
        className: String,
        expectedType: Class<T>
    ): T {
        val clazz = Class.forName(className, true, classLoader).asSubclass(expectedType)
        val constructor = clazz.getDeclaredConstructor()
        constructor.isAccessible = true
        return constructor.newInstance()
    }
}

internal data class LoadedPlugin(
    val classLoader: PluginClassLoader,
    val lifecycle: PluginLifecycle
)
