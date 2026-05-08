pluginManagement {
    includeBuild("sdk/gradle-extension/reactor-gradle-plugin")

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "Reactor"

include("kernel:micro")
include("kernel:api")

include("sdk:common")
include("sdk:bundled")

include("networking:api")
include("networking:protocol")
include("networking:internal")

include("launcher:runtime")
include("launcher:boostrap")
include("launcher:debug")

include("sdk:gradle-extension:shared")
include("sdk:gradle-extension:reactor-gradle-plugin")

file("example-plugins").listFiles()?.forEach { pluginDir ->
    if (pluginDir.isDirectory) {
        include("example-plugins:${pluginDir.name}")
    }
}
