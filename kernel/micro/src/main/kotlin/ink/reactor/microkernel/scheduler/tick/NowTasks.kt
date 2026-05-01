package ink.reactor.microkernel.scheduler.tick

internal class NowTasks {
    private var tasks = arrayOfNulls<() -> Unit>(16)
    private var size = 0

    fun addTask(task: () -> Unit) {
        ensureCapacity()
        tasks[size++] = task
    }

    fun drainTo(batch: TaskBatch) {
        if (size == 0) {
            return
        }

        for (i in 0 until size) {
            val task = tasks[i] ?: continue
            batch.add(task)
            tasks[i] = null
        }

        size = 0
    }

    fun cleanup() {
        if (size == 0) {
            return
        }

        tasks.fill(null, 0, size)
        size = 0
    }

    private fun ensureCapacity() {
        if (size < tasks.size) {
            return
        }

        tasks = tasks.copyOf(tasks.size * 2)
    }
}
