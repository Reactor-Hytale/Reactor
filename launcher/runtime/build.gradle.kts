import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.jvm.tasks.Jar
import java.util.UUID

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

fun existingProject(path: String): Project? {
    val project = rootProject.findProject(path)
    if (project == null) {
        logger.lifecycle("[Reactor-build] skipping missing project: $path")
    }
    return project
}

val publicProjects = publicProjectPaths.mapNotNull(::existingProject)
val internalProjects = internalProjectPaths.mapNotNull(::existingProject)

fun Project.jarTask() = tasks.named<Jar>("jar")
fun Project.jarFile(): Provider<RegularFile> = jarTask().flatMap { it.archiveFile }

/**
 * Gets only external dependencies from runtimeClasspath.
 * This excludes project dependencies, which we already handle ourselves
 * by manually merging the jars of the submodules.
 */
fun externalRuntimeFilesOf(projects: List<Project>) =
    projects.flatMap { project ->
        val runtimeClasspath = project.configurations.findByName("runtimeClasspath")
            ?: return@flatMap emptyList()

        runtimeClasspath.incoming
            .artifactView {
                componentFilter { id -> id is ModuleComponentIdentifier }
            }
            .files
            .files
            .toList()
    }.toSet()

val publicExternalRuntimeFiles: Provider<Set<File>> = providers.provider {
    externalRuntimeFilesOf(publicProjects)
}

val internalExternalRuntimeFiles: Provider<Set<File>> = providers.provider {
    externalRuntimeFilesOf(internalProjects) - publicExternalRuntimeFiles.get()
}

val publicJar by tasks.registering(ShadowJar::class) {
    group = "reactor"
    description = "Builds the embedded public.jar"

    dependsOn(publicProjects.map { it.jarTask() })

    archiveBaseName.set("public")
    archiveClassifier.set("")
    archiveVersion.set("")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates/public"))

    isZip64 = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from({
        publicProjects.map { project ->
            zipTree(project.jarFile().get().asFile)
        }
    })

    /*
     * Minimal runtime dependencies for the public loader to resolve its own classes.
     */
    from({
        publicExternalRuntimeFiles.get().map { file ->
            zipTree(file)
        }
    })

    mergeServiceFiles()

    exclude("META-INF/MANIFEST.MF")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("module-info.class")
}

val reactorJar by tasks.registering(ShadowJar::class) {
    group = "reactor"
    description = "Builds the final executable reactor.jar"

    dependsOn(tasks.named("classes"))
    dependsOn(publicJar)
    dependsOn(internalProjects.map { it.jarTask() })

    archiveBaseName.set("reactor")
    archiveClassifier.set("")
    archiveVersion.set("")
    destinationDirectory.set(layout.buildDirectory.dir("runtime"))

    isZip64 = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Runtime classes of the main module
    from(sourceSets.main.get().output)

    from({
        internalProjects.map { project ->
            zipTree(project.jarFile().get().asFile)
        }
    })

    from({
        internalExternalRuntimeFiles.get().map { file ->
            zipTree(file)
        }
    })

    into("embedded") {
        from(publicJar.flatMap { it.archiveFile }) {
            rename { "public.jar" }
        }
    }

    manifest {
        attributes(
            "Main-Class" to "codes.reactor.runtime.ReactorLauncher",
            "compile-id" to UUID.randomUUID().toString()
        )
    }

    mergeServiceFiles()

    exclude("META-INF/MANIFEST.MF")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("module-info.class")
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named("assemble") {
    dependsOn(reactorJar)
}

tasks.named("build") {
    dependsOn(reactorJar)
}

/*
 * Output: build/runtime/reactor.jar
 */
tasks.register("distRuntime") {
    group = "reactor"
    description = "Cleans and builds reactor.jar"

    dependsOn("clean")
    dependsOn(reactorJar)
}
