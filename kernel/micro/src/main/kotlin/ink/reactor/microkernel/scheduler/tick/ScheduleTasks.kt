package codes.reactor.microkernel.scheduler.tick

internal class ScheduleTasks {
    private class ScheduledRunnable(
        val id: Int,
        private val delegate: () -> Unit,
        val delay: Long,
        var countdown: Long
    ): Runnable {
        @Volatile
        var cancelled: Boolean = false

        override fun run() {
            if (!cancelled) {
                delegate.invoke()
            }
        }
    }

    private var tasks = arrayOfNulls<ScheduledRunnable>(16)

    private var taskIdCount = 0
    private var size = 0

    fun addTask(
        runnable: () -> Unit,
        startDelay: Long,
        delay: Long
    ): Int {
        ensureCapacity()

        val id = nextTaskId()

        tasks[size++] = ScheduledRunnable(
            id = id,
            delegate = runnable,
            delay = maxOf(0L, delay),
            countdown = maxOf(0L, startDelay)
        )

        return id
    }

    fun tickAndDrainTo(batch: TaskBatch) {
        if (size == 0) {
            return
        }

        var writeIndex = 0

        for (readIndex in 0 until size) {
            val task = tasks[readIndex] ?: continue

            if (task.cancelled) {
                continue
            }

            if (task.countdown > 0L) {
                task.countdown--
            } else {
                task.countdown = task.delay
                batch.add(task)
            }

            tasks[writeIndex++] = task
        }

        tasks.fill(null, writeIndex, size)
        size = writeIndex
    }

    fun removeTask(id: Int): Boolean {
        for (i in 0 until size) {
            val task = tasks[i] ?: continue

            if (task.id == id) {
                task.cancelled = true
                removeAt(i)
                return true
            }
        }

        return false
    }

    fun cleanup() {
        if (size == 0) {
            return
        }

        for (i in 0 until size) {
            tasks[i]?.cancelled = true
            tasks[i] = null
        }

        size = 0
    }

    private fun removeAt(index: Int) {
        val lastIndex = size - 1

        if (index < lastIndex) {
            System.arraycopy(
                tasks,
                index + 1,
                tasks,
                index,
                lastIndex - index
            )
        }

        tasks[lastIndex] = null
        size--
    }

    private fun ensureCapacity() {
        if (size < tasks.size) {
            return
        }

        tasks = tasks.copyOf(tasks.size * 2)
    }

    private fun nextTaskId(): Int {
        do {
            taskIdCount++
        } while (taskIdCount == 0)

        return taskIdCount
    }
}
