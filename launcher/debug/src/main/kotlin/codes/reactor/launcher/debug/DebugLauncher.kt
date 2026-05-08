package codes.reactor.launcher.debug

import codes.reactor.boostrap.ReactorBoostrap

fun main() {
    println("Debug Launcher Starting...")

    // No isolation in debug mode
    ReactorBoostrap.start(Thread.currentThread().contextClassLoader)
}
