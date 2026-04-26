package ink.reactor.microkernel.plugin.library.exception

import ink.reactor.kernel.plugin.exception.PluginLifecycleException

class PluginLibraryResolutionException(
    message: String,
    cause: Throwable? = null
) : PluginLifecycleException(message, cause)
