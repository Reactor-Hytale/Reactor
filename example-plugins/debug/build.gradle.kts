plugins {
    id("ink.reactor.plugin-metadata")
}

dependencies {
    compileOnly(project(":sdk:common"))
}

reactorPluginMetadata {
    packageName.set("ink.reactor.plugin.debug")
    /*
    * includeSubpackages.set(false)
    * failOnMultiplePlugins.set(true)
    * */
}
