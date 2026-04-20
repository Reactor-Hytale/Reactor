package ink.reactor.sdk.config

import ink.reactor.sdk.config.convert.ConfigConverters
import ink.reactor.sdk.config.delegate.ConfigDelegate
import ink.reactor.sdk.config.delegate.ConfigProperty
import ink.reactor.sdk.config.section.MapConfigSection
import kotlin.time.Duration

/**
 * Represents a named configuration section backed by a mutable key-value map.
 *
 * <p>A section may contain primitive values, collections, or nested sections represented
 * internally as nested maps.</p>
 */
interface ConfigSection {
    val name: String
    val data: MutableMap<String, Any?>

    operator fun get(key: String): Any? = data[key]
    operator fun set(key: String, value: Any?) { data[key] = value }

    infix fun String.to(value: Any?) {
        data[this] = value
    }

    fun getString(key: String): String? = get(key) as? String
    fun getInt(key: String): Int = (get(key) as? Number)?.toInt() ?: 0
    fun getInt(key: String, default: Int): Int = (get(key) as? Number)?.toInt() ?: default
    fun getLong(key: String): Long = (get(key) as? Number)?.toLong() ?: 0L
    fun getDouble(key: String): Double = (get(key) as? Number)?.toDouble() ?: 0.0
    fun getBoolean(key: String): Boolean = get(key) as? Boolean ?: false

    /**
     * Returns the value associated with the given key, or the provided default value
     * if the key is missing or the stored value cannot be cast to the requested type.
     *
     * @param T the expected value type
     * @param key the key to resolve
     * @param default the fallback value to return when resolution fails
     * @return the stored value cast to [T], or [default] if unavailable or incompatible
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getOrDefault(key: String, default: T): T {
        val value = data[key] ?: return default
        return try {
            value as T
        } catch (_: ClassCastException) {
            default
        }
    }

    /**
     * Returns the nested section stored at the given key, or {@code null} if the value
     * does not exist or is not backed by a mutable map.
     *
     * @param key the nested section key
     * @return the nested section, or {@code null} if no compatible section exists
     */
    @Suppress("UNCHECKED_CAST")
    fun getSection(key: String): ConfigSection? {
        return when (val value = data[key]) {
            is ConfigSection -> value
            is MutableMap<*, *> -> MapConfigSection(value as MutableMap<String, Any?>, key)
            is Map<*, *> -> {
                val mutableMap = mutableMapOf<String, Any?>()
                value.forEach { (k, v) -> mutableMap[k.toString()] = v }
                MapConfigSection(mutableMap, key)
            }
            else -> null
        }
    }

    /**
     * Returns the nested section stored at the given key, creating a new empty section
     * if it does not already exist.
     *
     * <p>If a compatible section is already present, it is returned as-is.</p>
     *
     * @param key the nested section key
     * @return an existing or newly created nested section
     */
    fun getOrCreateSection(key: String): ConfigSection {
        val existing = getSection(key)
        if (existing != null) {
            return existing
        }

        val newMap = LinkedHashMap<String, Any?>()
        data[key] = newMap
        return MapConfigSection(newMap, key)
    }

    /**
     * Creates a Kotlin property delegate backed by this section.
     *
     * <p>The delegate reads and writes values directly from this section. If the key is
     * absent or the stored value cannot be cast to the requested type, the provided
     * default value is returned.</p>
     *
     * <p>If [key] is not provided, the Kotlin property name is used.</p>
     *
     * @param T the delegated value type
     * @param default the fallback value returned when the stored value is missing or invalid
     * @param key optional explicit key; defaults to the property name
     * @return a config-backed property delegate
     */
    fun <T : Any> delegate(default: T, key: String? = null) = ConfigDelegate(this, default, key)

    fun boolean(default: Boolean, key: String? = null): ConfigProperty<Boolean> =
        ConfigProperty(this, key, default, ConfigConverters.BOOLEAN)

    fun int(default: Int, key: String? = null): ConfigProperty<Int> =
        ConfigProperty(this, key, default, ConfigConverters.INT)

    fun long(default: Long, key: String? = null): ConfigProperty<Long> =
        ConfigProperty(this, key, default, ConfigConverters.LONG)

    fun double(default: Double, key: String? = null): ConfigProperty<Double> =
        ConfigProperty(this, key, default, ConfigConverters.DOUBLE)

    fun string(default: String, key: String? = null): ConfigProperty<String> =
        ConfigProperty(this, key, default, ConfigConverters.STRING)

    fun unsignedInt(default: Int, key: String? = null): ConfigProperty<Int> =
        int(default, key).validate { it >= 0 }

    fun unsignedLong(default: Long, key: String? = null): ConfigProperty<Long> =
        long(default, key).validate { it >= 0 }

    fun duration(default: Duration, key: String? = null): ConfigProperty<Duration> =
        ConfigProperty(this, key, default, ConfigConverters.DURATION)

    fun duration(default: String, key: String? = null): ConfigProperty<Duration> =
        ConfigProperty(
            this,
            key,
            ConfigConverters.DURATION.convert(default) ?: Duration.ZERO,
            ConfigConverters.DURATION
        )

}
