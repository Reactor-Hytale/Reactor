plugins {
    id("codes.reactor.plugin-metadata")
}

dependencies {
    compileOnly(project(":sdk:common"))
}

reactorPluginMetadata {
    packageName.set("codes.reactor.plugin.discordhook")
    /*
    * includeSubpackages.set(false)
    * failOnMultiplePlugins.set(true)
    * */
}
