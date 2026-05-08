package codes.reactor.microkernel.plugin.library

import codes.reactor.kernel.plugin.library.Dependency
import codes.reactor.kernel.plugin.library.LibrariesRequest
import codes.reactor.kernel.plugin.library.Repository
import codes.reactor.microkernel.plugin.library.exception.PluginLibraryResolutionException
import codes.reactor.microkernel.plugin.validation.JarValidator.isValidJarFile
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class PluginLibraryResolver(
    private val libraryPathResolver: LibraryPathResolver,
    private val requestTimeout: Duration = Duration.ofSeconds(60),
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build(),
) {

    private val locks = ConcurrentHashMap<Path, Any>()

    fun resolve(request: LibrariesRequest): List<URL> {
        val dependencies = request.requestedDependencies

        if (dependencies.isEmpty()) {
            return emptyList()
        }

        val repositories = request.requestedRepositories.ifEmpty {
            linkedSetOf(Repository.MAVEN_CENTRAL)
        }

        return dependencies.map { dependency ->
            resolveDependency(dependency, repositories).toUri().toURL()
        }
    }

    private fun resolveDependency(
        dependency: Dependency,
        repositories: Collection<Repository>
    ): Path {
        val dependencyPath = libraryPathResolver.resolveDependencyFilePath(dependency)

        if (isValidJarFile(dependencyPath)) {
            return dependencyPath
        }

        val lock = locks.computeIfAbsent(dependencyPath) { Any() }
        synchronized(lock) {
            try {
                if (isValidJarFile(dependencyPath)) {
                    return dependencyPath
                }
                return tryDownloadFromRepositories(dependencyPath, repositories, dependency)
            } finally {
                locks.remove(dependencyPath)
            }
        }
    }

    private fun tryDownloadFromRepositories(
        dependencyPath: Path,
        repositories: Collection<Repository>,
        dependency: Dependency
    ): Path {
        val failures = mutableListOf<String>()

        for (repository in repositories) {
            val uri = libraryPathResolver.resolveHTTPDownloadURI(dependency, repository)
            val temporaryPath = dependencyPath.resolveSibling(
                dependencyPath.fileName.toString() + ".tmp-" + System.nanoTime()
            )

            try {
                downloadJar(uri, temporaryPath)
                moveAtomicallyOrReplace(temporaryPath, dependencyPath)
                return dependencyPath
            } catch (error: Exception) {
                failures += "${repository.url}: ${error.message ?: error::class.simpleName}"
                Files.deleteIfExists(temporaryPath)
            }
        }

        throw PluginLibraryResolutionException(
            buildString {
                appendLine("Could not resolve dependency '${dependency.notation}'.")
                appendLine("Tried repositories:")
                for (repository in repositories) {
                    appendLine(repository.url)
                }
                if (failures.isNotEmpty()) {
                    appendLine("Failures:")
                    for (failure in failures) {
                        appendLine("- $failure")
                    }
                }
            }
        )
    }

    private fun downloadJar(uri: URI, destination: Path) {
        destination.parent?.let { Files.createDirectories(it) }

        val request = HttpRequest.newBuilder(uri)
            .timeout(requestTimeout)
            .header("User-Agent", "Reactor-Library-Resolver")
            .GET()
            .build()

        val response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofFile(destination)
        )

        val statusCode = response.statusCode()

        if (statusCode != 200) {
            throw PluginLibraryResolutionException(
                "HTTP $statusCode while downloading $uri into $destination"
            )
        }

        if (!isValidJarFile(destination)) {
            throw PluginLibraryResolutionException(
                "Downloaded file is not a valid jar: $uri into $destination"
            )
        }
    }

    private fun moveAtomicallyOrReplace(
        source: Path,
        destination: Path
    ) {
        try {
            Files.move(
                source,
                destination,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(
                source,
                destination,
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }
}
