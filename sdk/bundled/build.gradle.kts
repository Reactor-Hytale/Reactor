plugins {
    id("java-library")
}
dependencies {
    api(project(":sdk:common"))
    implementation("org.snakeyaml:snakeyaml-engine:${findProperty("snakeYamlVersion")}")
}
