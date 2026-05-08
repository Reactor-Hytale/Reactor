package codes.reactor.microkernel.plugin.lifecycle.logger

import codes.reactor.kernel.logger.Logger
import codes.reactor.microkernel.config.KernelPluginConfig
import codes.reactor.microkernel.logger.plugin.ControlPluginLoggers
import codes.reactor.microkernel.plugin.catalog.PluginEntry

internal class PluginStartupLogController(
    private val config: KernelPluginConfig,
    private val logger: Logger
) {
    fun insert(entry: PluginEntry): Boolean {
        if (!config.logging.startupBuffer) {
            return false
        }

        val lifecycle = entry.lifecycle ?: return false
        if (ControlPluginLoggers.getLogger(lifecycle) != null) {
            return false
        }

        ControlPluginLoggers.insert(lifecycle, "[${entry.id}]")
        return true
    }

    fun hasStartupLogger(entry: PluginEntry): Boolean {
        val lifecycle = entry.lifecycle ?: return false
        return ControlPluginLoggers.getLogger(lifecycle) != null
    }

    fun flush(entry: PluginEntry) {
        entry.lifecycle?.let { ControlPluginLoggers.flush(it) }
    }

    fun discard(entry: PluginEntry) {
        entry.lifecycle?.let { ControlPluginLoggers.discard(it) }
    }

    fun info(entry: PluginEntry, message: String) {
        if (!config.logging.lifecycleEvents) {
            return
        }

        val lifecycle = entry.lifecycle
        val startupLogger = lifecycle?.let { ControlPluginLoggers.getLogger(it) }
        if (startupLogger != null) {
            startupLogger.info(message)
            return
        }

        logger.info("[${entry.id}] $message")
    }
}
