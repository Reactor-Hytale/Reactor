package codes.reactor.kernel.plugin.model.failure

data class PluginExceptionDetails(
    val type: String,
    val message: String? = null,
    val stackTrace: String? = null
) {
    companion object {
        fun from(
            throwable: Throwable,
            includeStackTrace: Boolean = false
        ): PluginExceptionDetails {
            return PluginExceptionDetails(
                type = throwable::class.qualifiedName ?: throwable::class.simpleName ?: "UnknownThrowable",
                message = throwable.message,
                stackTrace = if (includeStackTrace) throwable.stackTraceToString() else null
            )
        }
    }
}
