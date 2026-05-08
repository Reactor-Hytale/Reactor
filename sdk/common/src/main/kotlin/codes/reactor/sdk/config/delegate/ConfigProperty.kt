package codes.reactor.sdk.config.delegate

import codes.reactor.sdk.config.ConfigSection
import codes.reactor.sdk.config.convert.ConfigValueConverter
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Typed config-backed property delegate with support for conversion, validation,
 * and mapping.
 *
 * @param T the exposed property type
 */
class ConfigProperty<T : Any>(
    private val section: ConfigSection,
    private val customKey: String?,
    private val default: T,
    private val converter: ConfigValueConverter<T>,
    private val validators: List<(T) -> Boolean> = emptyList(),
    private val onInvalidValue: ((key: String, rawValue: Any?) -> Unit)? = null,
    private val onValidationFailure: ((key: String, value: T) -> Unit)? = null
) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val key = customKey ?: property.name
        val rawValue = section.data[key]

        val converted = converter.convert(rawValue)
        if (converted == null) {
            if (rawValue != null) {
                onInvalidValue?.invoke(key, rawValue)
            }
            return default
        }

        if (validators.any { !it(converted) }) {
            onValidationFailure?.invoke(key, converted)
            return default
        }

        return converted
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val key = customKey ?: property.name

        if (validators.any { !it(value) }) {
            onValidationFailure?.invoke(key, value)
            return
        }

        section.data[key] = value
    }

    /**
     * Returns a new property delegate with an additional validation rule.
     *
     * <p>If validation fails, the default value is returned on reads.</p>
     */
    fun validate(predicate: (T) -> Boolean): ConfigProperty<T> {
        return ConfigProperty(
            section = section,
            customKey = customKey,
            default = default,
            converter = converter,
            validators = validators + predicate,
            onInvalidValue = onInvalidValue,
            onValidationFailure = onValidationFailure
        )
    }

    /**
     * Returns a new property delegate that invokes the given callback when the raw config
     * value cannot be converted.
     */
    fun onInvalidValue(callback: (key: String, rawValue: Any?) -> Unit): ConfigProperty<T> {
        return ConfigProperty(
            section = section,
            customKey = customKey,
            default = default,
            converter = converter,
            validators = validators,
            onInvalidValue = callback,
            onValidationFailure = onValidationFailure
        )
    }

    /**
     * Returns a new property delegate that invokes the given callback when validation fails.
     */
    fun onValidationFailure(callback: (key: String, value: T) -> Unit): ConfigProperty<T> {
        return ConfigProperty(
            section = section,
            customKey = customKey,
            default = default,
            converter = converter,
            validators = validators,
            onInvalidValue = onInvalidValue,
            onValidationFailure = callback
        )
    }

    /**
     * Maps the resolved value of this property to a derived type.
     *
     * <p>The resulting delegate is read-only, since the mapped value cannot
     * be safely written back to the original config value in the general case.</p>
     */
    fun <R : Any> map(transform: (T) -> R): ReadOnlyProperty<Any?, R> {
        return MappedConfigProperty(
            base = this,
            transform = transform
        )
    }
}
