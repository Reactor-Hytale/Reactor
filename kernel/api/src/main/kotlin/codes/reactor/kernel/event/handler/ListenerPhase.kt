package codes.reactor.kernel.event.handler

/**
 * Defines the chronological phases for event listener execution.
 * <p>
 * Execution rules:
 * 1. Listeners are invoked in this sequence:
 *    INITIAL -> EARLY -> DEFAULT -> LATE -> FINAL -> MONITOR.
 * <p>
 * 2. Listeners with the same priority run in undefined order
 * (typically registration order).
 */
enum class ListenerPhase {
    /**
     * Runs in the initial phase (before EARLY).
     * Designed for setup that must precede all standard processing.
     */
    INITIAL,

    /**
     * Early phase (after INITIAL, before DEFAULT).
     * For modifications that should apply before most listeners.
     */
    EARLY,

    /**
     * Default phase. Used when no explicit priority is set.
     */
    DEFAULT,

    /**
     * Late phase (after DEFAULT, before FINAL).
     * For overrides or corrections to DEFAULT-priority logic.
     */
    LATE,

    /**
     * Final phase (after LATE, before MONITOR).
     * Last opportunity to modify the event state.
     */
    FINAL,

    /**
     * Observation-only phase (after all modifications).
     * <strong>Warning:</strong> Must never alter the event.
     */
    MONITOR
}
