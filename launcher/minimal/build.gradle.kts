plugins {
    kotlin("jvm") version "2.3.0"
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jline:jline-terminal:${findProperty("jlineVersion") ?: "3.30.4"}")
    implementation("org.jline:jline-reader:${findProperty("jlineVersion") ?: "3.30.4"}")

    compileOnly(project(":kernel:api"))
    compileOnly(project(":kernel:micro"))

    compileOnly(project(":sdk:bundled"))

    compileOnly(project(":networking:api"))
    compileOnly(project(":networking:protocol"))
    compileOnly(project(":networking:internal"))
}
