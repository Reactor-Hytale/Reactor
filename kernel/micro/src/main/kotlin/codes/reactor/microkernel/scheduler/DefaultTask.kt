package codes.reactor.microkernel.scheduler

import codes.reactor.kernel.scheduler.Scheduler
import codes.reactor.kernel.scheduler.Task

class DefaultTask(
    override val id: Int,

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
