package ink.reactor.sdk.util

/**
 * Utility class for placeholder replacement in strings
 */
object PlaceholderReplacement {

    @JvmStatic
    fun getReplacements(vararg replacements: Any): Map<String, String> {
        val map = HashMap<String, String>(replacements.size / 2)
        for (i in 0 until replacements.size - 1 step 2) {
            map[replacements[i].toString()] = replacements[i + 1].toString()
        }
        return map
    }

    @JvmStatic
    fun apply(message: String, vararg replacements: Any): String {
        if (replacements.isEmpty() || message.isEmpty()) {
            return message
        }
        return apply(message, getReplacements(*replacements))
    }

    @JvmStatic
    fun apply(message: String, replacements: Map<String, String>): String {
        if (replacements.isEmpty() || message.isEmpty()) {
            return message
        }

        val result = StringBuilder(message.length)
        val length = message.length
        var i = 0

        while (i < length) {
            val character = message[i]

            if (character == '%') {
                val end = message.indexOf('%', i + 1)

                if (end != -1) {
                    val key = message.substring(i + 1, end)
                    val replacement = replacements[key]

                    if (replacement != null) {
                        result.append(replacement)
                        i = end + 1
                        continue
                    }
                }
            }

            result.append(character)
            i++
        }

        return result.toString()
    }
}
