plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}

val publicProjectPaths = listOf(
    ":kernel:api",
    ":networking:api",
    ":sdk:common"
)

val internalProjectPaths = listOf(
    ":kernel:micro",
    ":networking:protocol",
    ":networking:internal",
    ":sdk:bundled",
    ":launcher:minimal"
)

dependencies {
    publicProjectPaths.forEach { path ->
        val project = rootProject.findProject(path)
        if (project != null) {
            implementation(project)
        } else {
            logger.lifecycle("[Reactor-debug-build] skipping missing project: $path")
        }
    }

    internalProjectPaths.forEach { path ->
        val project = rootProject.findProject(path)
        if (project != null) {
            implementation(project)
        } else {
            logger.lifecycle("[Reactor-debug-build] skipping missing project: $path")
        }
    }
}
