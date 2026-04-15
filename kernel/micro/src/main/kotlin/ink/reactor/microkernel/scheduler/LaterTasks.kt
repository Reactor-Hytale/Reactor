package ink.reactor.microkernel.scheduler

import java.util.Arrays

internal class LaterTasks {
    private var runnables = arrayOfNulls<Runnable>(16)
    private var delays = LongArray(16)
    private var size = 0

    fun addTask(task: Runnable, delay: Long) {
        if (size >= runnables.size) {
            val newSize = runnables.size * 2
            runnables = runnables.copyOf(newSize)
            delays = delays.copyOf(newSize)
        }
        runnables[size] = task
        delays[size] = delay
        size++
    }

    fun executeAll() {
        if (size == 0) {
            return
        }

        var writeIndex = 0
        for (readIndex in 0..size) {
            val task = runnables[readIndex] ?: continue

            if (delays[readIndex] <= 0) {
                task.run()
                runnables[readIndex] = null
                continue
            }

            delays[readIndex]--
            if (writeIndex != readIndex) {
                runnables[writeIndex] = runnables[readIndex]
                delays[writeIndex] = delays[readIndex]
                runnables[readIndex] = null
            }
            writeIndex++
        }
        size = writeIndex
    }

    fun cleanup() {
        Arrays.fill(delays, 0)
        Arrays.fill(runnables, null)
    }
}
