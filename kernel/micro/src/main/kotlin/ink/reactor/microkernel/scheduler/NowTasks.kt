package ink.reactor.microkernel.scheduler

import java.util.Arrays

internal class NowTasks {
    private var tasks = arrayOfNulls<Runnable>(16)
    private var backTasks = arrayOfNulls<Runnable>(16)
    private var pos = 0

    fun addTask(task: Runnable) {
        if (pos >= tasks.size) {
            tasks = tasks.copyOf(tasks.size * 2)
            backTasks = arrayOfNulls(tasks.size)
        }
        tasks[pos++] = task
    }

    fun executeAll() {
        if (pos == 0) {
            return
        }

        val temp = tasks
        val currentPos = pos

        tasks = backTasks
        pos = 0

        for (i in 0..currentPos) {
            temp[i]?.run()
            temp[i] = null
        }

        backTasks = temp
    }

    fun cleanup() {
        Arrays.fill(backTasks, 0)
        Arrays.fill(tasks, null)
    }
}
