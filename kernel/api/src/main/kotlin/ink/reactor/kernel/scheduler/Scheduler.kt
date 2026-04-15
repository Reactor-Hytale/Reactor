package ink.reactor.kernel.scheduler

/**
 * A tick-based scheduler for executing tasks at specific ticks or after delays.
 */
interface Scheduler {

    /**
     * Executes the task immediately in the current tick.
     * @param task The task to execute.
     */
    fun runNow(task: () -> Unit)

    /**
     * Schedules a task to execute at an exact tick number.
     * Example (tickToExecute = 2):
     * <p> Tick 1: No execution  </p>
     * <p> Tick 2: Task executes </p>
     *
     * @param task The task to schedule (non-null).
     * @param tickToExecute The absolute tick number for execution (e.g., 2 = tick 2).
     */
    fun runAtTick(task: () -> Unit, tickToExecute: Ticks)

    /**
     * Schedules a repeating task to execute at fixed tick intervals, starting immediately.
     * Example (executeInTheTick = 2):
     * <p> Tick 1: First execution   </p>
     * <p> Tick 2: No execution      </p>
     * <p> Tick 3: Second execution  </p>
     *
     * @param task The task to schedule (non-null).
     * @param executeInTheTick Fixed interval between executions (e.g., 3 = every 3 ticks).
     * @return A unique task ID for cancellation.
     */
    fun scheduleEvery(task: () -> Unit, executeInTheTick: Ticks): Int {
        return scheduleEvery(task, Ticks.ZERO, executeInTheTick)
    }

    /**
     * Schedules a repeating task to execute at fixed tick intervals, starting at a specific tick.
     * Example (tickToStart = 2, executeInTheTick = 3):
     * <p> Tick 1: No execution             </p>
     * <p> Tick 2: First execution          </p>
     * <p> Tick 5: Second execution (2 + 3) </p>
     * <p> Tick 8: Third execution (5 + 3)  </p>
     *
     * @param task The task to schedule (non-null).
     * @param tickToStart Absolute tick for first execution (e.g., 2 = tick 2).
     * @param executeInTheTick Fixed interval between executions (e.g., 3 = every 3 ticks).
     * @return A unique task ID for cancellation.
     */
    fun scheduleEvery(task: () -> Unit, tickToStart: Ticks, executeInTheTick: Ticks): Int

    /**
     * Schedules a task to execute after a delay (relative to current tick).
     * Example (delay = 2):
     * <p> Tick 1: No execution  </p>
     * <p> Tick 2: No execution  </p>
     * <p> Tick 3: Task executes </p>
     *
     * @param task The task to schedule (non-null).
     * @param delay Ticks to wait before execution (e.g., 2 = execute after 2 ticks).
     */
    fun runAfterDelay(task: () -> Unit, delay: Ticks)

    /**
     * Schedules a repeating task with a fixed delay between executions, starting immediately.
     * Example (repeat = 3):
     * <p> Tick 1: First execution               </p>
     * <p> Tick 2: No execution                  </p>
     * <p> Tick 3: No execution                  </p>
     * <p> Tick 4: No execution                  </p>
     * <p> Tick 7: Second execution (delay = 3) </p>
     *
     * @param task The task to schedule (non-null).
     * @param delayBetweenExecute Ticks to wait between executions (e.g., 3 = every 3 ticks).
     * @return A unique task ID for cancellation.
     */
    fun scheduleWithDelayBetween(task: () -> Unit, delayBetweenExecute: Ticks): Int {
        return scheduleWithDelayBetween(task, Ticks.ZERO, delayBetweenExecute)
    }

    /**
     * Schedules a repeating task with a delay before the first execution and between subsequent executions.
     * Example (delayFirstExecute = 2, delayBetweenExecute = 3):
     * <p> Tick 1: No execution                  </p>
     * <p> Tick 2: No execution                  </p>
     * <p> Tick 3: First execution (delay = 2)   </p>
     * <p> Tick 4: No execution                  </p>
     * <p> Tick 5: No execution                  </p>
     * <p> Tick 6: No execution                  </p>
     * <p> Tick 7: Second execution (delay = 3)  </p>
     *
     * @param task The task to schedule (non-null).
     * @param delayFirstExecute Ticks to wait before first execution (e.g., 2 = after 2 ticks).
     * @param delayBetweenExecute Ticks to wait between executions (e.g., 3 = every 3 ticks; 0 = no repetition).
     * @return A unique task ID for cancellation.
     */
    fun scheduleWithDelayBetween(task: () -> Unit, delayFirstExecute: Ticks, delayBetweenExecute: Ticks): Int

    /**
     * Cancels a scheduled task.
     * @param taskId The ID returned when scheduling the task.
     * @return True if the task was found and canceled, false otherwise.
     */
    fun cancelScheduleTask(taskId: Int): Boolean

    /**
     * Shuts down the scheduler, optionally canceling pending tasks.
     * @param cancelPendingTasks If true, all pending tasks will be canceled. If false, they may still execute if the scheduler allows it.
     */
    fun shutdown(cancelPendingTasks: Boolean = true)
}
