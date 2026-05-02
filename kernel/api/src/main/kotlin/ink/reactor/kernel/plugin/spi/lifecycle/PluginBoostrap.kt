package codes.reactor.kernel.plugin.spi.lifecycle

import codes.reactor.kernel.plugin.library.LibrariesRequest

interface PluginBoostrap {
    fun boot(libraries: LibrariesRequest)
}
