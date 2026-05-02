package codes.reactor.microkernel.plugin.classloading

import codes.reactor.kernel.plugin.model.PluginId
import codes.reactor.microkernel.plugin.scope.PluginsScopeContainer
import java.net.URL
import java.net.URLClassLoader

internal class PluginClassLoader(
    val id: PluginId,
    urls: Array<URL>,
    private val dependencyClassLoaders: List<ClassLoader>,
    parentClassLoader: ClassLoader
) : URLClassLoader(id.toString(), urls, parentClassLoader) {

    companion object {
        init {
            registerAsParallelCapable()
        }
    }

    override fun close() {
        PluginsScopeContainer.closeScope(this)
        super.close()
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        synchronized(getClassLoadingLock(name)) {
            val alreadyLoaded = findLoadedClass(name)
            if (alreadyLoaded != null) {
                if (resolve) {
                    resolveClass(alreadyLoaded)
                }
                return alreadyLoaded
            }

            val fromPublicParent = runCatching { parent.loadClass(name) }.getOrNull()
            if (fromPublicParent != null) {
                if (resolve) {
                    resolveClass(fromPublicParent)
                }
                return fromPublicParent
            }

            for (dependencyClassLoader in dependencyClassLoaders) {
                val fromDependency = runCatching { dependencyClassLoader.loadClass(name) }.getOrNull()
                if (fromDependency != null) {
                    if (resolve) {
                        resolveClass(fromDependency)
                    }
                    return fromDependency
                }
            }

            val fromPlugin = findClass(name)
            if (resolve) {
                resolveClass(fromPlugin)
            }
            return fromPlugin
        }
    }

    override fun toString(): String {
        return "ID: " + id + " - " + super.toString()
    }
}
