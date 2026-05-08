package codes.reactor.microkernel.plugin.library

import codes.reactor.kernel.plugin.library.Dependency
import codes.reactor.kernel.plugin.library.Repository
import java.nio.file.Path
import java.net.URI

class LibraryPathResolver(val parentPath: Path) {

    private fun buildGroupPath(dependency: Dependency): String {
        return dependency.group.replace('.', '/')
    }

    private fun buildArtifactPath(dependency: Dependency): String {
        val groupPath = buildGroupPath(dependency)
        return "$groupPath/${dependency.artifact}/${dependency.version}"
    }

    private fun buildFileName(dependency: Dependency): String {
        return "${dependency.artifact}-${dependency.version}.jar"
    }

    fun resolveDependencyFolder(dependency: Dependency): Path {
        return parentPath.resolve(buildArtifactPath(dependency))
    }

    fun resolveDependencyFilePath(dependency: Dependency): Path {
        return resolveDependencyFolder(dependency).resolve(buildFileName(dependency))
    }

    // Example: https://repo.maven.apache.org/maven2/org/apache/commons/commons-lang3/3.20.0/commons-lang3-3.20.0.jar
    fun resolveHTTPDownloadURI(dependency: Dependency, repository: Repository): URI {
        val baseUrl = repository.url.let { if (it.endsWith("/")) it else "$it/" }
        val urlString = "$baseUrl${buildArtifactPath(dependency)}/${buildFileName(dependency)}"
        return URI(urlString)
    }
}
