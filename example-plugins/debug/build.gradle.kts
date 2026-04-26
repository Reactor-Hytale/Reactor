plugins {
    id("ink.reactor.plugin-metadata")
}

dependencies {
    compileOnly(project(":sdk:common"))
    compileOnly("org.apache.commons:commons-lang3:3.20.0")
}

reactorPluginMetadata {
    packageName.set("ink.reactor.plugin.debug")
    /*
    * includeSubpackages.set(false)
    * failOnMultiplePlugins.set(true)
    * */
}
