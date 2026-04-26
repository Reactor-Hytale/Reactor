package ink.reactor.microkernel.plugin.lifecycle

import ink.reactor.kernel.logger.Logger
import ink.reactor.kernel.plugin.exception.PluginLifecycleExecutionException
import ink.reactor.kernel.plugin.exception.PluginLoadException
import ink.reactor.kernel.plugin.exception.PluginOperationInProgressException
import ink.reactor.kernel.plugin.exception.PluginTransitionNotAllowedException
import ink.reactor.kernel.plugin.model.failure.PluginExceptionDetails
import ink.reactor.kernel.plugin.model.failure.PluginFailure
import ink.reactor.kernel.plugin.model.lifecycle.PluginState
import ink.reactor.microkernel.plugin.lifecycle.runtime.PluginInstanceCreator
import ink.reactor.microkernel.plugin.lifecycle.runtime.PluginRuntimeReleaser
import ink.reactor.microkernel.plugin.lifecycle.logger.PluginStartupLogController
import ink.reactor.microkernel.plugin.lifecycle.runtime.StartupLogEnd
import ink.reactor.microkernel.plugin.catalog.PluginEntry
import ink.reactor.microkernel.plugin.lifecycle.runtime.withPluginClassLoader
import java.util.concurrent.TimeUnit

internal class PluginLifecycleRunner(
    private val instanceCreator: PluginInstanceCreator,
    private val releaser: PluginRuntimeReleaser,
    private val startupLogs: PluginStartupLogController,
    private val logger: Logger
) {
    fun load(entry: PluginEntry, keepStartupLogger: Boolean) {
        if (!beginLoad(entry)) {
            return
        }

        var insertedStartupLogger = false
        val startedAt = System.nanoTime()

        try {
            val loadedPlugin = instanceCreator.create(entry)

            synchronized(entry) {
                if (entry.state == PluginState.CANCELLED) {
                    runCatching { loadedPlugin.classLoader.close() }
                    throw cancelledException(entry, PluginState.LOADING, "Plugin startup was cancelled before onLoad.")
                }

                entry.classLoader = loadedPlugin.classLoader
                entry.lifecycle = loadedPlugin.lifecycle
            }

            insertedStartupLogger = startupLogs.insert(entry)

            withPluginClassLoader(loadedPlugin.classLoader) {
                loadedPlugin.lifecycle.onLoad()
            }

            finishLoad(entry)
            startupLogs.info(entry, "Loaded in ${elapsedMillis(startedAt)}ms.")
        } catch (error: Throwable) {
            if (isCancellation(error)) {
                requestCancel(
                    entry = entry,
                    phase = PluginState.LOADING,
                    reason = "Plugin startup was cancelled during LOADING.",
                    cause = error
                )
                releaser.release(entry, StartupLogEnd.DISCARD)
                throw asCancellation(entry, PluginState.LOADING, error)
            }

            fail(entry, PluginState.LOADING, error)
            throw PluginLifecycleExecutionException(entry.id, PluginState.LOADING, error)
        } finally {
            if (insertedStartupLogger && !keepStartupLogger && entry.state == PluginState.LOADED) {
                startupLogs.flush(entry)
            }
        }
    }

    fun enable(entry: PluginEntry) {
        if (!beginEnable(entry)) {
            return
        }

        val startedAt = System.nanoTime()

        try {
            val lifecycle = entry.lifecycle
                ?: throw PluginLoadException("Plugin ${entry.id} has no lifecycle instance.")
            val classLoader = entry.classLoader
                ?: throw PluginLoadException("Plugin ${entry.id} has no classloader.")

            startupLogs.insert(entry)

            withPluginClassLoader(classLoader) {
                lifecycle.onEnable()
            }

            finishEnable(entry)
            startupLogs.info(entry, "Enabled in ${elapsedMillis(startedAt)}ms.")
            startupLogs.flush(entry)
        } catch (error: Throwable) {
            if (isCancellation(error)) {
                requestCancel(
                    entry = entry,
                    phase = PluginState.ENABLING,
                    reason = "Plugin startup was cancelled during ENABLING.",
                    cause = error
                )
                releaser.release(entry, StartupLogEnd.DISCARD)
                throw asCancellation(entry, PluginState.ENABLING, error)
            }

            fail(entry, PluginState.ENABLING, error)
            throw PluginLifecycleExecutionException(entry.id, PluginState.ENABLING, error)
        }
    }

    fun disable(entry: PluginEntry) {
        val shouldCallDisable = beginDisable(entry) ?: return
        val startedAt = System.nanoTime()

        try {
            val lifecycle = entry.lifecycle
            val classLoader = entry.classLoader

            if (shouldCallDisable && lifecycle != null && classLoader != null) {
                withPluginClassLoader(classLoader) {
                    lifecycle.onDisable()
                }
            }

            synchronized(entry) {
                entry.state = PluginState.DISABLED
                entry.failure = null
            }

            startupLogs.info(entry, "Disabled in ${elapsedMillis(startedAt)}ms.")
        } catch (error: Throwable) {
            fail(entry, PluginState.DISABLING, error)
            throw PluginLifecycleExecutionException(entry.id, PluginState.DISABLING, error)
        } finally {
            releaser.release(entry, StartupLogEnd.FLUSH)
        }
    }

    fun requestCancel(
        entry: PluginEntry,
        phase: PluginState,
        reason: String,
        cause: Throwable? = null
    ): Boolean {
        val changed = synchronized(entry) {
            if (entry.state == PluginState.CANCELLED) {
                false
            } else if (entry.state == PluginState.LOADING ||
                entry.state == PluginState.ENABLING ||
                entry.state == PluginState.WAITING_FOR_DEPENDENCIES
            ) {
                entry.state = PluginState.CANCELLED
                entry.failure = PluginFailure(
                    state = PluginState.CANCELLED,
                    exception = PluginExceptionDetails(
                        "PluginStartupTimeout",
                        reason
                    )
                )
                true
            } else {
                false
            }
        }

        if (changed) {
            val suffix = cause?.let { " Cause: ${it.javaClass.simpleName}" } ?: ""
            logger.warn("[${entry.id}] cancelled during $phase.$suffix")
        }

        return changed
    }

    fun fail(entry: PluginEntry, phase: PluginState, error: Throwable) {
        synchronized(entry) {
            entry.state = PluginState.FAILED
            entry.failure = PluginFailure(
                state = phase,
                exception = PluginExceptionDetails.from(error, includeStackTrace = true)
            )
        }

        if (phase == PluginState.LOADING || phase == PluginState.ENABLING || phase == PluginState.FAILED) {
            releaser.release(entry, StartupLogEnd.FLUSH)
        }

        logger.error("Plugin ${entry.id} failed during $phase.", error)
    }

    private fun beginLoad(entry: PluginEntry): Boolean {
        return synchronized(entry) {
            when (entry.state) {
                PluginState.LOADED,
                PluginState.ENABLED -> false

                PluginState.LOADING,
                PluginState.ENABLING,
                PluginState.DISABLING -> {
                    throw PluginOperationInProgressException(entry.id, entry.state, PluginState.LOADING)
                }

                PluginState.WAITING_FOR_DEPENDENCIES,
                PluginState.DISABLED,
                PluginState.FAILED,
                PluginState.SKIPPED,
                PluginState.CANCELLED -> {
                    entry.state = PluginState.LOADING
                    entry.failure = null
                    true
                }
            }
        }
    }

    private fun finishLoad(entry: PluginEntry) {
        throwIfCancelled(entry, PluginState.LOADING)

        synchronized(entry) {
            if (entry.state == PluginState.CANCELLED) {
                throw cancelledException(entry, PluginState.LOADING, "Plugin startup was cancelled after onLoad.")
            }

            entry.state = PluginState.LOADED
            entry.failure = null
        }
    }

    private fun beginEnable(entry: PluginEntry): Boolean {
        return synchronized(entry) {
            when (entry.state) {
                PluginState.ENABLED,
                PluginState.CANCELLED -> false

                PluginState.LOADED -> {
                    entry.state = PluginState.ENABLING
                    true
                }

                PluginState.LOADING,
                PluginState.ENABLING,
                PluginState.DISABLING -> {
                    throw PluginOperationInProgressException(entry.id, entry.state, PluginState.ENABLING)
                }

                else -> throw PluginTransitionNotAllowedException(entry.state, PluginState.ENABLING)
            }
        }
    }

    private fun finishEnable(entry: PluginEntry) {
        throwIfCancelled(entry, PluginState.ENABLING)

        synchronized(entry) {
            if (entry.state == PluginState.CANCELLED) {
                throw cancelledException(entry, PluginState.ENABLING, "Plugin startup was cancelled after onEnable.")
            }

            entry.state = PluginState.ENABLED
            entry.failure = null
        }
    }

    private fun beginDisable(entry: PluginEntry): Boolean? {
        return synchronized(entry) {
            when (entry.state) {
                PluginState.DISABLED,
                PluginState.WAITING_FOR_DEPENDENCIES,
                PluginState.SKIPPED,
                PluginState.CANCELLED -> null

                PluginState.LOADING,
                PluginState.ENABLING,
                PluginState.DISABLING -> {
                    throw PluginOperationInProgressException(entry.id, entry.state, PluginState.DISABLING)
                }

                PluginState.LOADED -> {
                    entry.state = PluginState.DISABLING
                    false
                }

                PluginState.ENABLED,
                PluginState.FAILED -> {
                    entry.state = PluginState.DISABLING
                    true
                }
            }
        }
    }

    private fun throwIfCancelled(entry: PluginEntry, phase: PluginState) {
        if (Thread.currentThread().isInterrupted) {
            throw cancelledException(entry, phase, "Plugin startup thread was interrupted during $phase.")
        }
    }

    private fun isCancellation(error: Throwable): Boolean {
        return error is PluginStartupCancelledException || PluginInterruption.isInterruption(error)
    }

    private fun asCancellation(
        entry: PluginEntry,
        phase: PluginState,
        error: Throwable
    ): PluginStartupCancelledException {
        if (error is PluginStartupCancelledException) {
            return error
        }

        return cancelledException(entry, phase, "Plugin startup was cancelled during $phase.", error)
    }

    private fun cancelledException(
        entry: PluginEntry,
        phase: PluginState,
        message: String,
        cause: Throwable? = null
    ): PluginStartupCancelledException {
        return PluginStartupCancelledException(entry.id, phase, message, cause)
    }

    private fun elapsedMillis(startedAt: Long): Long {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)
    }
}
