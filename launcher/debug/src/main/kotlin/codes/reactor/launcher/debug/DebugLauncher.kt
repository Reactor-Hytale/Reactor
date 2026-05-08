package codes.reactor.launcher.debug

import codes.reactor.launcher.MinimalReactorLauncher

fun main() {
    println("Debug Launcher Starting...")

    // No isolation in debug mode
    MinimalReactorLauncher.start(Thread.currentThread().contextClassLoader)
}
