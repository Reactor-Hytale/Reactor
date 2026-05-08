package codes.reactor.sdk.bundled.config.yaml

import codes.reactor.sdk.config.section.MapConfigSection
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.constructor.StandardConstructor
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.Tag

internal class YamlCustomConstructor(settings: LoadSettings) : StandardConstructor(settings) {

    init {
        this.tagConstructors[Tag.MAP] = ConstructMapAsSection()
    }

    private inner class ConstructMapAsSection : ConstructYamlMap() {
        override fun construct(node: Node): Any {
            val mappingNode = node as MappingNode
            val result = LinkedHashMap<String, Any?>()

            mappingNode.value.forEach { tuple ->
                val key = constructObject(tuple.keyNode)?.toString() ?: "null"
                val value = constructObject(tuple.valueNode)
                result[key] = value
            }

            return MapConfigSection(result)
        }

        override fun constructRecursive(node: Node, `object`: Any) {
            if (`object` is MapConfigSection) {
                super.constructRecursive(node, `object`.data)
            }
        }
    }
}
