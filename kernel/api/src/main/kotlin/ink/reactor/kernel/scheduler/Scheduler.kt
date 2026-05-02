package codes.reactor.kernel.scheduler

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A time-based scheduler for executing tasks at specific moments or after defined delays.
 */
interface Scheduler {

    /**
     * The smallest time increment supported by this scheduler.
     * Example: If a tick-scheduler executes a tick every 50ms, this unit is 50ms.
     */
    val minPrecisionUnit: Duration

    /**
     * Executes the task immediately in the current execution cycle.
     * @param task The task to execute.
     */
    fun runNow(task: () -> Unit)

    /**
     * Schedules a task to execute at an exact duration from the scheduler's start.
     * Example (timeToExecute = 2s):
     * <p> 1.0s: No execution </p>
     * <p> 2.0s: Task executes </p>
     *
     * @param task The task to schedule.
     * @param timeToExecute The absolute duration from start for execution (e.g., 2s).
     */
    fun runAt(task: () -> Unit, timeToExecute: Duration)

    /**
     * Schedules a repeating task to execute at fixed intervals, starting immediately.
     * Example (interval = 2s):
     * <p> 0s: First execution  </p>
     * <p> 2s: Second execution </p>
     * <p> 4s: Third execution  </p>
     *
     * @param task The task to schedule.
     * @param interval Fixed interval between executions (e.g., 3s = every 3 seconds).
     * @return A unique task ID for cancellation.
     */
    fun scheduleEvery(task: () -> Unit, interval: Duration): Task {
        return scheduleEvery(task, 0.seconds, interval)
    }

    /**
     * Schedules a repeating task to execute at fixed intervals, starting at a specific duration
     * from the scheduler's start.
     *
     * Example (startTime = 2s, interval = 3s):
     * - T=2s: First execution
     * - T=5s: Second execution (2s + 3s)
     * - T=8s: Third execution (5s + 3s)
     *
     * @param task The task to schedule.
     * @param startTime Absolute duration from start for the first execution.
     * @param interval Fixed interval between executions.
     * @return A unique task ID for cancellation.
     */
    fun scheduleEvery(task: () -> Unit, startTime: Duration, interval: Duration): Task

    /**
     * Schedules a task to execute after a specific delay relative to the current time.
     * Example (delay = 2s):
     * <p> 0s: Task scheduled    </p>
     * <p> 1s: No execution      </p>
     * <p> 2s: Task executes     </p>
     *
     * @param task The task to schedule.
     * @param delay Duration to wait before execution.
     */
    fun runAfterDelay(task: () -> Unit, delay: Duration)

    /**
     * Schedules a repeating task with a fixed delay between executions, starting immediately.
     * Example (delay = 3s):
     * <p> 0s: First execution                  </p>
     * <p> 3s: Second execution (after 3s delay) </p>
     *
     * @param task The task to schedule.
     * @param delayBetweenExecute Duration to wait between executions.
     * @return A unique task ID for cancellation.
     */
    fun scheduleWithDelay(task: () -> Unit, delayBetweenExecute: Duration): Task {
        return scheduleWithDelay(task, 0.seconds, delayBetweenExecute)
    }

    /**
     * Schedules a repeating task with a delay before the first execution and between subsequent executions.
     * Example (initialDelay = 2s, delayBetweenExecute = 3s):
     * <p> 1s: No execution                      </p>
     * <p> 2s: First execution (after 2s delay)   </p>
     * <p> 5s: Second execution (after 3s delay)  </p>
     *
     * @param task The task to schedule.
     * @param initialDelay Duration to wait before the first execution.
     * @param delayBetweenExecute Duration to wait between subsequent executions.
     * @return A unique task ID for cancellation.
     */
    fun scheduleWithDelay(task: () -> Unit, initialDelay: Duration, delayBetweenExecute: Duration): Task

    /**
     * Cancels a scheduled task.
     * @param taskId The ID returned when the task was scheduled.
     * @return True if the task was found and canceled, false otherwise.
     */
    fun cancelTask(taskId: Int): Boolean

    /**
     * Shuts down the scheduler.
     * @param cancelPendingTasks If true, all pending tasks will be canceled.
     */
    fun shutdown(cancelPendingTasks: Boolean = true)
}
