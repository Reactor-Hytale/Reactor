package ink.reactor.sdk.config.section

import ink.reactor.sdk.config.ConfigSection

class MapConfigSection(
    override val data: MutableMap<String, Any?> = LinkedHashMap(),
    override val name: String = "",
) : ConfigSection {

    override fun toString(): String {
        return "name:$name, data:$data"
    }
}
