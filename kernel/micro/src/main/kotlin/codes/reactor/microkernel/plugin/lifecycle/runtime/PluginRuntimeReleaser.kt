package codes.reactor.microkernel.plugin.lifecycle.runtime

import codes.reactor.kernel.logger.Logger
import codes.reactor.microkernel.plugin.lifecycle.logger.PluginStartupLogController
import codes.reactor.microkernel.plugin.catalog.PluginEntry

internal enum class StartupLogEnd {
    FLUSH,
    DISCARD,
    NONE
}

internal class PluginRuntimeReleaser(
    private val logger: Logger,
    private val startupLogs: PluginStartupLogController
) {
    fun release(entry: PluginEntry, startupLogEnd: StartupLogEnd = StartupLogEnd.FLUSH) {
        when (startupLogEnd) {
            StartupLogEnd.FLUSH -> startupLogs.flush(entry)
            StartupLogEnd.DISCARD -> startupLogs.discard(entry)
            StartupLogEnd.NONE -> Unit
        }

        val classLoader = entry.classLoader
        entry.lifecycle = null
        entry.classLoader = null

        runCatching { classLoader?.close() }
            .onFailure { error -> logger.error("Could not close classloader for plugin ${entry.id}.", error) }
    }
}
