package ink.reactor.kernel.logger

/**
 * Registers and monitors loggers to detect every time their logging methods
 * such as `info()`, `debug()`, `warn()`, `error()`, etc. are called.
 *
 * This class acts as a "spy" that intercepts calls to logger methods
 * without interfering with their normal behavior. It is useful for:
 *  * Auditing when certain information is logged
 *  * Real-time debugging and tracing of events
 *  * Logging metrics or statistics
 *  * Testing and log verification
 *
 * **Important:** Registered loggers are automatically
 * cleaned up when the plugin closes to prevent memory leaks. @see{PluginScope}
 */
interface LoggerSpy {
    /**
     * Registers a logger to monitor ALL its logging calls
     * (info, debug, warn, error, etc.).
     *
     * @param logger the logger to monitor
     */
    fun register(logger: Logger)

    /**
     * Removes ALL registered loggers and releases all references from this scope.
     */
    fun unregister()
}
