package ink.reactor.kernel.plugin.model

@JvmInline
value class PluginId(val value: String) {
    init {
        require(value.length <= 32) { "Plugin id cannot be longer than 32 characters." } // For readability and to prevent abuse.
        require(value.isNotBlank()) { "Plugin id cannot be blank." }
        require(ID_PATTERN.matches(value)) {
            "Plugin id '$value' only allows lowercase letters, digits, dots, underscores and hyphens, and must start with a letter or digit."
        }
    }

    override fun toString(): String = value

    private companion object {
        val ID_PATTERN = Regex("^[a-z0-9][a-z0-9._-]*$")
    }
}
