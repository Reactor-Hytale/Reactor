package ink.reactor.sdk.config.convert

import kotlin.time.Duration

/**
 * Built-in converters used by typed config delegates.
 */
object ConfigConverters {

    val BOOLEAN = ConfigValueConverter { value ->
        when (value) {
            is Boolean -> value
            is String -> when (value.trim().lowercase()) {
                "true", "yes", "y", "1", "on" -> true
                "false", "no", "n", "0", "off" -> false
                else -> null
            }
            is Number -> value.toInt() != 0
            else -> null
        }
    }

    val INT = ConfigValueConverter { value ->
        when (value) {
            is Int -> value
            is Number -> value.toInt()
            is String -> value.trim().toIntOrNull()
            else -> null
        }
    }

    val LONG = ConfigValueConverter { value ->
        when (value) {
            is Long -> value
            is Number -> value.toLong()
            is String -> value.trim().toLongOrNull()
            else -> null
        }
    }

    val DOUBLE = ConfigValueConverter { value ->
        when (value) {
            is Double -> value
            is Number -> value.toDouble()
            is String -> value.trim().toDoubleOrNull()
            else -> null
        }
    }

    val STRING = ConfigValueConverter { value ->
        when (value) {
            null -> null
            is String -> value
            else -> value.toString()
        }
    }

    val DURATION = ConfigValueConverter { value ->
        when (value) {
            equals("permanent") -> Duration.INFINITE
            else -> Duration.parseOrNull(value.toString()) ?: Duration.ZERO
        }
    }
}
