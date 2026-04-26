package ink.reactor.launcher.debug

import ink.reactor.launcher.MinimalReactorLauncher

fun main() {
    println("Debug Launcher Starting...")

    // No isolation in debug mode
    MinimalReactorLauncher.start(Thread.currentThread().contextClassLoader)
}
