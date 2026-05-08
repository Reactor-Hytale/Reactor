package codes.reactor.sdk.bundled.config.yaml

import codes.reactor.sdk.config.ConfigSection

class SectionToYamlConverter(private val indentSpaces: Int) {
    private val builder = StringBuilder()
    private var indentLevel = 0

    fun toYaml(section: ConfigSection): String {
        appendMap(section.data)
        return builder.toString()
    }

    private fun appendMap(map: Map<String, Any?>) {
        map.entries.forEachIndexed { index, entry ->
            val (key, value) = entry

            builder.append(" ".repeat(indentLevel * indentSpaces))
            builder.append(key).append(": ")

            appendObject(value)

            if (index < map.size - 1) builder.append("\n")
        }
    }

    private fun appendObject(value: Any?) {
        when (value) {
            null -> builder.append("null")
            is Boolean, is Number -> builder.append(value)
            is ConfigSection -> {
                builder.append("\n")
                indentLevel++
                appendMap(value.data)
                indentLevel--
            }
            is Map<*, *> -> {
                builder.append("\n")
                indentLevel++
                @Suppress("UNCHECKED_CAST")
                appendMap(value as Map<String, Any?>)
                indentLevel--
            }
            is Collection<*> -> appendCollection(value)
            else -> {
                val escaped = value.toString()
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                builder.append("\"").append(escaped).append("\"")
            }
        }
    }

    private fun appendCollection(collection: Collection<*>) {
        if (collection.isEmpty()) {
            builder.append("[]")
            return
        }
        builder.append("\n")
        collection.forEachIndexed { index, item ->
            builder.append(" ".repeat(indentLevel * indentSpaces)).append("- ")
            indentLevel++
            appendObject(item)
            indentLevel--
            if (index < collection.size - 1) builder.append("\n")
        }
    }
}
