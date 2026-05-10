package codes.reactor.sdk.bundled.scanner

import codes.reactor.kernel.logger.Logger
import codes.reactor.sdk.bundled.scanner.injector.DependencyInjector
import codes.reactor.sdk.scanner.PackageScanner
import codes.reactor.sdk.scanner.ScanResult
import codes.reactor.sdk.scanner.registrar.ObjectRegistrar
import java.io.File
import java.lang.reflect.Modifier
import java.net.JarURLConnection
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.LinkedHashSet

/**
 * Default manual implementation of [PackageScanner].
 *
 * It uses a [DependencyInjector] to instantiate discovered classes and
 * registers them through the configured [ObjectRegistrar] instances.
 *
 * Classes are filtered by the registrar target type before instantiation,
 * avoiding unnecessary object creation.
 */
class DefaultPackageScanner(
    private val classLoader: ClassLoader,
    private val logger: Logger,
    override val defaultPackage: String,
) : PackageScanner {

    private data class Registration(
        val registrar: ObjectRegistrar<*>,
        val packages: List<String>
    )

    private val injector = DependencyInjector()
    private val registrations = mutableListOf<Registration>()

    override fun <T : Any> provide(instance: T) {
        injector.register(instance)
    }

    override fun provide(vararg instances: Any) {
        instances.forEach { instance -> injector.register(instance) }
    }

    override fun <T : Any> provideAs(type: Class<in T>, instance: T) {
        injector.register(type, instance)
    }

    override fun <T : Any> provideWithHierarchy(instance: T) {
        injector.registerWithHierarchy(instance)
    }

    override fun registrar(instance: ObjectRegistrar<*>, vararg packages: String) {
        val resolvedPackages = if (packages.isEmpty()) {
            listOf(defaultPackage)
        } else {
            packages.toList()
        }

        registrations += Registration(instance, resolvedPackages)
    }

    override fun scan(): ScanResult {
        val results = mutableListOf<ScanResult.PackageScanResult>()
        val time = System.currentTimeMillis()
        for (registration in registrations) {
            for (packageName in registration.packages) {
                val discovered = findClasses(packageName)
                if (discovered.isEmpty()) {
                    logger.debug("Can't match any class in the $packageName")
                    continue
                }

                val accepted = mutableListOf<Class<*>>()
                for (clazz in discovered) {
                    if (!registration.registrar.targetType.isAssignableFrom(clazz)) {
                        continue
                    }

                    val instance = instantiateCandidate(clazz) ?: continue
                    accepted += clazz

                    @Suppress("UNCHECKED_CAST")
                    (registration.registrar as ObjectRegistrar<Any>).registrar(instance)
                }
                results += ScanResult.PackageScanResult(
                    classes = accepted,
                    packageName = packageName,
                    registrar = registration.registrar
                )

                logger.debug("Accepted ${accepted.size} classes: $accepted")
            }
        }

        if (results.isEmpty()) {
            logger.debug("ScanResult is empty. No classes found")
        }
        logger.debug("Scanned in ${System.currentTimeMillis() - time} ms")

        return ScanResult(results)
    }

    private fun findClasses(packageName: String): Collection<Class<*>> {
        val path = packageName.replace('.', '/')
        val resources = classLoader.getResources(path)
        val classes = LinkedHashSet<Class<*>>()

        while (resources.hasMoreElements()) {
            val url = resources.nextElement()

            when (url.protocol) {
                "file" -> scanDirectory(
                    packageName = packageName,
                    directory = File(URLDecoder.decode(url.path, StandardCharsets.UTF_8)),
                    out = classes
                )

                "jar" -> scanJar(
                    rootPath = path,
                    url = url,
                    out = classes
                )
            }
        }

        return classes
    }

    private fun scanDirectory(
        packageName: String,
        directory: File,
        out: MutableSet<Class<*>>
    ) {
        if (!directory.exists()) return

        directory.walkTopDown()
            .filter { it.isFile && it.extension == "class" }
            .forEach { file ->
                val relative = file.relativeTo(directory).invariantSeparatorsPath
                    .removeSuffix(".class")

                if ('$' in relative) return@forEach

                val className = buildString {
                    append(packageName)
                    if (relative.isNotBlank()) {
                        append('.')
                        append(relative.replace('/', '.'))
                    }
                }

                loadCandidate(className)?.let(out::add)
            }
    }

    private fun scanJar(
        rootPath: String,
        url: java.net.URL,
        out: MutableSet<Class<*>>
    ) {
        val connection = url.openConnection()
        if (connection !is JarURLConnection) return

        connection.jarFile.use { jar ->
            val prefix = "$rootPath/"

            val entries = jar.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name

                if (entry.isDirectory) continue
                if (!name.startsWith(prefix)) continue
                if (!name.endsWith(".class")) continue

                val className = name.removeSuffix(".class").replace('/', '.')
                if ('$' in className) continue

                loadCandidate(className)?.let(out::add)
            }
        }
    }

    private fun loadCandidate(className: String): Class<*>? {
        return runCatching {
            val clazz = Class.forName(className, false, classLoader)

            if (clazz.isInterface || clazz.isAnnotation || clazz.isEnum) return null
            if (Modifier.isAbstract(clazz.modifiers)) return null
            if (clazz.isAnonymousClass || clazz.isLocalClass || clazz.isSynthetic) return null

            clazz
        }.getOrNull()
    }

    private fun instantiateCandidate(clazz: Class<*>): Any? {
        loadKotlinObjectSingleton(clazz)?.let { return it }

        return runCatching {
            injector.createInstance(clazz)
        }.onFailure {
            logger.error("Failed to instantiate $clazz", it)
        }.getOrNull()
    }

    private fun loadKotlinObjectSingleton(clazz: Class<*>): Any? {
        return runCatching {
            val field = clazz.getDeclaredField("INSTANCE")
            if (!Modifier.isStatic(field.modifiers)) return null
            if (!field.type.isAssignableFrom(clazz)) return null

            field.isAccessible = true
            field.get(null)
        }.getOrNull()
    }
}
