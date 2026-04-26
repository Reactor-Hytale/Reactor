package ink.reactor.kernel.plugin.model

import ink.reactor.kernel.plugin.library.LibrariesRequest

data class BoostrapContext(
    val librariesRequest: LibrariesRequest,
) {

    inline fun BoostrapContext.request(
        block: BoostrapContext.() -> Unit
    ) {
        block()
    }
}
