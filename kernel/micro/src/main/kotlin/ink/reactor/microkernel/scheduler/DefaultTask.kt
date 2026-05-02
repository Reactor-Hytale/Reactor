package ink.reactor.microkernel.scheduler

import ink.reactor.kernel.scheduler.Scheduler
import ink.reactor.kernel.scheduler.Task

class DefaultTask(
    override val id: Long,

    @Volatile
    override var canceled: Boolean,

    @Volatile
    var scheduler: Scheduler?,
) : Task {

    override fun cancel() {
        if (!canceled) {
            scheduler?.cancelTask(id)
            scheduler = null
            canceled = true
        }
    }
}
