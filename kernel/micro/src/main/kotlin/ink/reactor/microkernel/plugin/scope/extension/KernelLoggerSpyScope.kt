package ink.reactor.microkernel.plugin.scope.extension

import ink.reactor.kernel.logger.Logger
import ink.reactor.kernel.logger.LoggerSpy
import ink.reactor.kernel.plugin.scope.PluginDependencyProvider
import ink.reactor.kernel.plugin.scope.PluginScopeFactory
import ink.reactor.microkernel.plugin.scope.KernelPluginScope
import java.util.Collections

class KernelLoggerSpyScope(
    private val pluginScopeFactory: PluginScopeFactory
): LoggerSpy {
    private val loggers = Collections.synchronizedSet(mutableSetOf<Logger>())

    override fun register(logger: Logger) {
        val scope = pluginScopeFactory.acquire()

        if (loggers.add(logger) && scope is KernelPluginScope) {
            scope.manage(MultipleLoggerSpyHolder(this))
        }
    }

    override fun unregister() {
        loggers.clear()
    }

    fun getLoggers() = loggers

    private class MultipleLoggerSpyHolder(
        private val spyScope: KernelLoggerSpyScope
    ): PluginDependencyProvider<MultipleLoggerSpyHolder> {
        private val loggers = Collections.synchronizedSet(mutableSetOf<Logger>())

        override fun provide(): MultipleLoggerSpyHolder {
            return this
        }

        override fun close() {
            spyScope.loggers.removeAll(this.loggers)
            this.loggers.clear()
        }
    }
}
