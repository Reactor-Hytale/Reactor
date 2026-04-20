package ink.reactor.sdk.config.delegate

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Read-only mapped view over a [ConfigProperty].
 *
 * @param T the source type
 * @param R the mapped type
 */
class MappedConfigProperty<T : Any, R : Any>(
    private val base: ConfigProperty<T>,
    private val transform: (T) -> R,
    private val validators: List<(R) -> Boolean> = emptyList(),
    private val onValidationFailure: ((key: String, value: R) -> Unit)? = null
) : ReadOnlyProperty<Any?, R> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): R {
        val value = transform(base.getValue(thisRef, property))

        if (validators.any { !it(value) }) {
            onValidationFailure?.invoke(property.name, value)
        }

        return value
    }

    /**
     * Returns a new mapped property with an additional validation rule.
     */
    fun validate(predicate: (R) -> Boolean): MappedConfigProperty<T, R> {
        return MappedConfigProperty(
            base = base,
            transform = transform,
            validators = validators + predicate,
            onValidationFailure = onValidationFailure
        )
    }

    /**
     * Returns a new mapped property that invokes the given callback when validation fails.
     */
    fun onValidationFailure(callback: (key: String, value: R) -> Unit): MappedConfigProperty<T, R> {
        return MappedConfigProperty(
            base = base,
            transform = transform,
            validators = validators,
            onValidationFailure = callback
        )
    }
}
