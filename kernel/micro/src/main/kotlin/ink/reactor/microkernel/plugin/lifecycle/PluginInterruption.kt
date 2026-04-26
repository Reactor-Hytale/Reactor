package ink.reactor.microkernel.plugin.lifecycle

internal object PluginInterruption {
    fun isInterruption(error: Throwable): Boolean {
        var current: Throwable? = error
        while (current != null) {
            if (current is InterruptedException) {
                return true
            }
            current = current.cause
        }
        return false
    }
}
