package codes.reactor.microkernel.plugin.library.exception

import codes.reactor.kernel.plugin.exception.PluginLifecycleException

class PluginLibraryResolutionException(
    message: String,
    cause: Throwable? = null
) : PluginLifecycleException(message, cause)
