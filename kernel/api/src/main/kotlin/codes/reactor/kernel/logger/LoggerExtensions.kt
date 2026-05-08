package codes.reactor.kernel.logger

import codes.reactor.kernel.Reactor
import kotlin.reflect.KClass

fun Any.logger(): Logger =
    Reactor.loggerFactory.acquire(this)

fun KClass<*>.logger(): Logger =
    Reactor.loggerFactory.acquire(this)

inline fun <reified T : Any> logger(): Logger =
    Reactor.loggerFactory.acquire(T::class)
