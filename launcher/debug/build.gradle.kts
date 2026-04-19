plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}

val publicProjectPathsStr = findProperty("publicProjectPaths").toString()
val internalProjectPathsStr = findProperty("internalProjectPaths").toString()

val publicProjectPaths = publicProjectPathsStr.split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }

val internalProjectPaths = internalProjectPathsStr.split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }

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
