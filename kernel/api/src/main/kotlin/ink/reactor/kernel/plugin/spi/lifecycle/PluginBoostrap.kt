package ink.reactor.kernel.plugin.spi.lifecycle

import ink.reactor.kernel.plugin.library.LibrariesRequest

interface PluginBoostrap {
    fun boot(libraries: LibrariesRequest)
}
