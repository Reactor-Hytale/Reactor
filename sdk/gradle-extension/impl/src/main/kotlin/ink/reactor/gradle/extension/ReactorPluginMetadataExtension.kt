package codes.reactor.gradle.extension

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class ReactorPluginMetadataExtension @Inject constructor(
    objects: ObjectFactory
) {
    val packageName: Property<String> = objects.property(String::class.java)

    val includeSubpackages: Property<Boolean> =
        objects.property(Boolean::class.java).convention(false)

    val pluginAnnotationClass: Property<String> =
        objects.property(String::class.java)
            .convention("codes.reactor.sdk.plugin.annotation.Plugin")

    val bootstrapAnnotationClass: Property<String> =
        objects.property(String::class.java)
            .convention("codes.reactor.sdk.plugin.annotation.Bootstrap")

    val bootstrapAnnotationAliasClass: Property<String> =
        objects.property(String::class.java)
            .convention("codes.reactor.sdk.plugin.annotation.Boostrap")

    val outputFileName: Property<String> =
        objects.property(String::class.java)
            .convention("manifest.properties")

    val failOnMultiplePlugins: Property<Boolean> =
        objects.property(Boolean::class.java).convention(false)
}
