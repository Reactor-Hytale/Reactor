package codes.reactor.kernel.scheduler

import kotlin.time.Duration

fun Scheduler.after(delay: Duration, task: () -> Unit) {
    this.runAfterDelay(task, delay)
}

fun Scheduler.at(time: Duration, task: () -> Unit) {
    this.runAt(task, time)
}

fun Scheduler.every(interval: Duration, task: () -> Unit): Task {
    return this.scheduleEvery(task, interval)
}

fun Scheduler.every(
    interval: Duration,
    startingIn: Duration = Duration.ZERO,
    task: () -> Unit
): Task {
    return this.scheduleEvery(task, startingIn, interval)
}
