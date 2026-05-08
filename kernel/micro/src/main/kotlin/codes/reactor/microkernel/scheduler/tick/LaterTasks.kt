package codes.reactor.microkernel.scheduler.tick

internal class LaterTasks {
    private var runnables = arrayOfNulls<() -> Unit>(16)
    private var delays = LongArray(16)
    private var size = 0

    fun addTask(task: () -> Unit, delay: Long) {
        ensureCapacity()

        runnables[size] = task
        delays[size] = delay
        size++
    }

    fun tickAndDrainTo(batch: TaskBatch) {
        if (size == 0) {
            return
        }

        var writeIndex = 0

        for (readIndex in 0 until size) {
            val task = runnables[readIndex] ?: continue
            val delay = delays[readIndex]

            if (delay <= 0L) {
                batch.add(task)
                continue
            }

            runnables[writeIndex] = task
            delays[writeIndex] = delay - 1L
            writeIndex++
        }

        runnables.fill(null, writeIndex, size)
        delays.fill(0L, writeIndex, size)

        size = writeIndex
    }

    fun cleanup() {
        if (size == 0) {
            return
        }

        runnables.fill(null, 0, size)
        delays.fill(0L, 0, size)

        size = 0
    }

    private fun ensureCapacity() {
        if (size < runnables.size) {
            return
        }

        val newSize = runnables.size * 2

        runnables = runnables.copyOf(newSize)
        delays = delays.copyOf(newSize)
    }
}
