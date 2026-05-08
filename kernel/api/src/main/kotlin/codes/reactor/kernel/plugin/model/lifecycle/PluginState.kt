package codes.reactor.kernel.plugin.model.lifecycle

enum class PluginState {
    /**
     * The plugin is ready to proceed, but it is currently blocked waiting for
     * one or more required dependencies to reach a compatible state.
     */
    WAITING_FOR_DEPENDENCIES,

    /**
     * The plugin is currently executing its load phase.
     *
     * This phase is typically used for internal preparation and may run in
     * parallel with unrelated plugins when dependency rules allow it.
     */
    LOADING,

    /**
     * The plugin completed its load phase successfully.
     *
     * The plugin is prepared for activation, but it is not yet active.
     */
    LOADED,

    /**
     * The plugin is currently executing its enable phase.
     *
     * During this phase, the plugin becomes operational and may publish
     * services, register listeners, or start tasks.
     */
    ENABLING,

    /**
     * The plugin is fully active and operational.
     */
    ENABLED,

    /**
     * The plugin is currently executing its disable phase.
     */
    DISABLING,

    /**
     * The plugin has been disabled successfully and is no longer operational.
     *
     * Depending on the runtime policy, it may later be re-enabled or unloaded.
     */
    DISABLED,

    /**
     * The plugin encountered an unrecoverable failure during one of its lifecycle
     * phases or due to an external runtime condition.
     *
     * Failure details should be stored separately, including the phase, cause,
     * and root exception.
     */
    FAILED,

    /**
     * The plugin was intentionally not started by the runtime.
     *
     * This may happen because it is incompatible, disabled by configuration,
     * rejected by policy, or excluded during startup.
     */
    SKIPPED,

    /**
     * The plugin lifecycle operation was canceled before completion.
     *
     * This may happen during shutdown, timeout handling, dependency failure
     * propagation, or explicit administrative interruption.
     */
    CANCELLED
}
