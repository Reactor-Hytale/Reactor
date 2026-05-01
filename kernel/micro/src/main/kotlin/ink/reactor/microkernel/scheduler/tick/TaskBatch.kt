package ink.reactor.microkernel.scheduler.tick

internal class TaskBatch(
    initialCapacity: Int = 16
) {
    private var tasks = arrayOfNulls<Runnable>(initialCapacity)
    private var size = 0

    fun add(task: Runnable) {
        ensureCapacity()
        tasks[size++] = task
    }

    fun clear() {
        if (size == 0) {
            return
        }

        tasks.fill(null, 0, size)
        size = 0
    }

    fun runAndClear() {
        var firstFailure: Throwable? = null

        try {
            for (i in 0 until size) {
                val task = tasks[i] ?: continue
                tasks[i] = null

                try {
                    task.run()
                } catch (failure: Throwable) {
                    val first = firstFailure

                    if (first == null) {
                        firstFailure = failure
                    } else {
                        first.addSuppressed(failure)
                    }
                }
            }
        } finally {
            clear()
        }

        firstFailure?.let { throw it }
    }

    private fun ensureCapacity() {
        if (size < tasks.size) {
            return
        }

        val newSize = if (tasks.isEmpty()) {
            16
        } else {
            tasks.size * 2
        }

        tasks = tasks.copyOf(newSize)
    }
}
