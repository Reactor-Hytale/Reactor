package ink.reactor.microkernel.plugin.classloading

import ink.reactor.kernel.plugin.model.PluginId
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
}
