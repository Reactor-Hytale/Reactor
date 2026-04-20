package ink.reactor.sdk.config.convert

/**
 * Converts raw config values to a target type.
 *
 * @param T the target type
 */
fun interface ConfigValueConverter<T : Any> {
    /**
     * Converts the given raw value to the target type.
     *
     * @param value the raw config value
     * @return the converted value, or {@code null} if conversion is not possible
     */
    fun convert(value: Any?): T?
}
