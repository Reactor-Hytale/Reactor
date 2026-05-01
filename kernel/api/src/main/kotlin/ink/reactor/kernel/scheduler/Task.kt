package ink.reactor.kernel.scheduler

interface Task {
    val id: Int
    val canceled: Boolean

    fun cancel()
}
