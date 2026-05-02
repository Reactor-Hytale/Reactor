package codes.reactor.plugin.debug

import codes.reactor.kernel.logger.logger
import codes.reactor.kernel.plugin.library.LibrariesRequest
import codes.reactor.kernel.plugin.library.Repository
import codes.reactor.kernel.plugin.spi.lifecycle.PluginBoostrap
import codes.reactor.sdk.plugin.annotation.Bootstrap

@Bootstrap
class DebugPluginBoostrap: PluginBoostrap {

    override fun boot(libraries: LibrariesRequest) {
        this.logger().info("Loading libraries")
        libraries.apply {
            repository(Repository.MAVEN_CENTRAL)
            // Example custom repo:
            // repository(name = "Best repo", url = "https://repo.bestrepoever.com/")

            // You can define dependencies with this format:
            dependency("org.apache.commons:commons-lang3:3.20.0")
            /* Or this:
            dependency(
                group = "org.apache.commons",
                artifact = "commons-lang3",
                version = "3.20.0"
            )*/
        }
    }
}
