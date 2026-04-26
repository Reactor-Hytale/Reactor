dependencies {
    implementation(kotlin("stdlib"))

    val jline = findProperty("jlineVersion")
    implementation("org.jline:jline-terminal:$jline")
    implementation("org.jline:jline-reader:$jline")

    compileOnly(project(":kernel:api"))
    compileOnly(project(":kernel:micro"))

    compileOnly(project(":sdk:bundled"))

    compileOnly(project(":networking:api"))
    compileOnly(project(":networking:protocol"))
    compileOnly(project(":networking:internal"))
}
