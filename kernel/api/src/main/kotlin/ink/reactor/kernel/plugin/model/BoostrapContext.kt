package codes.reactor.kernel.plugin.model

import codes.reactor.kernel.plugin.library.LibrariesRequest

data class BoostrapContext(
    val librariesRequest: LibrariesRequest,
) {

    inline fun BoostrapContext.request(
        block: BoostrapContext.() -> Unit
    ) {
        block()
    }
}
