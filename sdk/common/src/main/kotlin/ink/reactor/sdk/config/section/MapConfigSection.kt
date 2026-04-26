package ink.reactor.sdk.config.section

import ink.reactor.sdk.config.ConfigSection

class MapConfigSection(
    override val data: MutableMap<String, Any?> = LinkedHashMap(),
    override val name: String = "",
) : ConfigSection {

    override fun toString(): String {
        return "name:$name, data:$data"
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getList(key: String, clazz: Class<T>): MutableList<T> {
        val value = get(key)
        if (value !is MutableList<*>) {
            return mutableListOf()
        }
        value.removeIf { next: Any? -> next == null || next.javaClass != clazz }
        return value as MutableList<T>
    }
}
