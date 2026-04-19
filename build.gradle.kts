plugins {
    java
    kotlin("jvm") version "2.3.0"
}

group = "ink.reactor"
version = "1.0.0"

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:${findProperty("jetbrainsAnnotationsVersion")}")

        testImplementation("org.junit.jupiter:junit-jupiter:${findProperty("junitJupiterVersion")}")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:${findProperty("junitPlatformVersion")}")

        testImplementation(kotlin("test"))
    }

    tasks.test {
        useJUnitPlatform()
    }
}

allprojects {
    apply<JavaPlugin>()

    val javaToolchainVersion = (findProperty("javaToolchain") as String).toInt()

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(javaToolchainVersion)
    }

    configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaToolchainVersion))
    }

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(javaToolchainVersion)
    }
}
