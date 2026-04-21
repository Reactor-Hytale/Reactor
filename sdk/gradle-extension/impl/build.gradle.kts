plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
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
            id = "ink.reactor.plugin-metadata"
            implementationClass = "ink.reactor.gradle.ReactorGradlePlugin"
            displayName = "Reactor Plugin Metadata"
            description = "Generates plugin.properties from @Plugin annotation"
        }
    }
}
