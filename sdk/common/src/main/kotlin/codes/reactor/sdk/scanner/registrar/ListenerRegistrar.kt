package codes.reactor.sdk.scanner.registrar

import codes.reactor.kernel.Reactor
import codes.reactor.sdk.extension.eventbus

object ListenerRegistrar : ObjectRegistrar<Any> {

    override val targetType = Any::class.java

    override fun registrar(instance: Any) {
        val scope = Reactor.pluginScopeFactory.acquire()
        scope.eventbus().subscribe(instance)
    }
}
