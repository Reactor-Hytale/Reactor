package codes.reactor.sdk.scanner.registrar

interface ObjectRegistrar<T : Any> {

    val targetType: Class<T>

    fun registrar(instance: T)
}
