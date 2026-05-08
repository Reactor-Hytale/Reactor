plugins {
    `java-gradle-plugin`
    id("org.gradle.kotlin.kotlin-dsl") version "6.5.2"
}
repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.ow2.asm:asm:9.9.1")

    implementation(gradleApi())
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("reactorPluginMetadata") {
            id = "codes.reactor.plugin-metadata"
            implementationClass = "codes.reactor.gradle.ReactorGradlePlugin"
            displayName = "Reactor Plugin Metadata"
            description = "Generates plugin.properties from @Plugin annotation"
        }
    }
}
