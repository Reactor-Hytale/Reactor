package codes.reactor.sdk.config.delegate

import codes.reactor.sdk.config.ConfigSection
import kotlin.reflect.KProperty

/**
 * Property delegate backed by a [codes.reactor.sdk.config.ConfigSection].
 *
 * <p>If the target key is missing or its value cannot be cast to the expected type,
 * the provided default value is returned instead.</p>
 *
 * @param T the delegated value type
 * @param section the backing config section
 * @param default the fallback value returned when the key is absent or incompatible
 * @param customKey optional explicit config key; when omitted, the Kotlin property name is used
 */
class ConfigDelegate<T : Any>(
    private val section: ConfigSection,
    private val default: T,
    private val customKey: String? = null
) {
    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val key = customKey ?: property.name
        return section.data[key] as? T ?: default
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val key = customKey ?: property.name
        section.data[key] = value
    }
}
