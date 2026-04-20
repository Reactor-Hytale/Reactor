package ink.reactor.sdk.util

import java.util.concurrent.TimeUnit

object TimeFormatter {

    private val UNIT_MULTIPLIERS = mapOf(
        's' to 1L,
        'm' to 60L,
        'h' to 3600L,
        'd' to 86400L,
        'M' to 2592000L,
        'a' to 31536000L
    )

    private val FORMAT_UNITS = listOf(
        'y' to TimeUnit.DAYS.toMillis(365),
        'M' to TimeUnit.DAYS.toMillis(30),
        'd' to TimeUnit.DAYS.toMillis(1),
        'h' to TimeUnit.HOURS.toMillis(1),
        'm' to TimeUnit.MINUTES.toMillis(1),
        's' to TimeUnit.SECONDS.toMillis(1)
    )

    fun format(seconds: Long): String {
        if (seconds < 60) return "${seconds}s"

        val years = seconds / 31536000L
        val months = (seconds % 31536000L) / 2592000L
        val days = (seconds % 2592000L) / 86400L
        val hours = (seconds % 86400L) / 3600L
        val minutes = (seconds % 3600L) / 60L

        return when {
            years > 0 -> "${years}y ${days}d"
            months > 0 -> "${months}m ${days}d"
            days > 0 -> if (hours == 0L) "${days}d" else "${days}d ${hours}h"
            hours > 0 -> if (minutes == 0L) "${hours}h" else "${hours}h ${minutes}m"
            else -> {
                val sec = seconds % 60
                if (sec == 0L) "${minutes}m" else "${minutes}m ${sec}s"
            }
        }
    }

    fun formatMillis(inputMillis: Long, includeMillis: Boolean = false): String {
        if (inputMillis == 0L) return "0s"

        val result = mutableListOf<String>()
        var remaining = inputMillis

        for ((char, unitMillis) in FORMAT_UNITS) {
            val count = remaining / unitMillis
            if (count > 0) {
                result.add("$count$char")
                remaining %= unitMillis
            }
        }

        if (includeMillis && remaining > 0) {
            result.add("${remaining}ms")
        }

        return result.joinToString(" ")
    }

    fun parseToSeconds(part: String): Long {
        return parseToSeconds(part.split(" "))
    }

    fun parseToSeconds(parts: Collection<String>): Long {
        if (parts.any { it.equals("permanent", ignoreCase = true) }) return -1L

        var totalSeconds = 0L
        for (part in parts) {
            if (part.length < 2) throw IllegalArgumentException("Invalid format: $part")

            val unit = part.last()
            val multiplier = UNIT_MULTIPLIERS[unit]
                ?: throw IllegalArgumentException("Invalid unit: $unit")

            val value = part.substring(0, part.length - 1).toLongOrNull()
                ?: throw IllegalArgumentException("Invalid number: $part")

            totalSeconds += value * multiplier
        }
        return totalSeconds
    }
}
