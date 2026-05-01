package ink.reactor.kernel.scheduler.tick

import ink.reactor.kernel.scheduler.Scheduler
import ink.reactor.kernel.scheduler.Task

interface TickDrivenScheduler : Scheduler {
    val currentTick: Ticks

    fun tick()
    fun tick(amount: Ticks)

    /**
     * Schedules a task to execute at an exact tick number.
     * Example (tickToExecute = 2):
     * <p> Tick 1: No execution  </p>
     * <p> Tick 2: Task executes </p>
     *
     * @param task The task to schedule (non-null).
     * @param tickToExecute The absolute tick number for execution (e.g., 2 = tick 2).
     */
    fun runAtTick(task: () -> Unit, tickToExecute: Ticks);

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
    fun runAfterDelay(task: () -> Unit, delay: Ticks);

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
    fun scheduleEvery(task: () -> Unit, tickToStart: Ticks, executeInTheTick: Ticks): Task;

    /**
     * Schedules a repeating task with a fixed delay between executions, starting immediately.
     * Example (repeat = 3):
     * <p> Tick 1: First execution               </p>
     * <p> Tick 2: No execution                  </p>
     * <p> Tick 3: No execution                  </p>
     * <p> Tick 4: No execution                  </p>
     * <p> Tick 5: Second execution (delay = 3) </p>
     *
     * @param task The task to schedule (non-null).
     * @param delayBetweenExecute Ticks to wait between executions (e.g., 3 = every 3 ticks).
     * @return A unique task for cancellation.
     */
    fun scheduleWithDelayBetween(task: () -> Unit, delayFirstExecute: Ticks, delayBetweenExecute: Ticks): Task
}
