package ink.reactor.gradle.scanner

import ink.reactor.gradle.model.ScanResult
import ink.reactor.gradle.model.ScannedClassInfo
import ink.reactor.gradle.scanner.asm.PluginClassAnnotationVisitor
import org.objectweb.asm.ClassReader
import java.io.File

class PluginClassScanner(
    private val pluginAnnotationClass: String,
    private val bootstrapAnnotationClass: String,
    private val bootstrapAnnotationAliasClass: String
) {

    fun scan(
        classRoots: Collection<File>,
        targetPackage: String,
        includeSubpackages: Boolean
    ): ScanResult {
        val classFiles = classRoots
            .filter { it.exists() }
            .flatMap { root ->
                if (root.isDirectory) {
                    root.walkTopDown()
                        .filter { it.isFile && it.extension == "class" }
                        .toList()
                } else {
                    emptyList()
                }
            }
            .sortedBy { it.absolutePath }

        val scanned = classFiles
            .map { scanClass(it) }
            .filter { info ->
                isInsideTargetPackage(
                    classPackage = info.packageName,
                    targetPackage = targetPackage,
                    includeSubpackages = includeSubpackages
                )
            }

        val pluginClasses = scanned.filter { it.hasPlugin }
        val bootstrapClass = scanned.firstOrNull { it.hasBootstrap }

        return ScanResult(
            pluginClass = pluginClasses.firstOrNull(),
            bootstrapClass = bootstrapClass,
            allPluginClasses = pluginClasses
        )
    }

    private fun scanClass(classFile: File): ScannedClassInfo {
        val reader = ClassReader(classFile.readBytes())
        val visitor = PluginClassAnnotationVisitor(
            pluginDescriptor = toDescriptor(pluginAnnotationClass),
            bootstrapDescriptors = setOf(
                toDescriptor(bootstrapAnnotationClass),
                toDescriptor(bootstrapAnnotationAliasClass)
            )
        )

        reader.accept(
            visitor,
            ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
        )

        return visitor.toScannedClassInfo()
    }

    private fun isInsideTargetPackage(
        classPackage: String,
        targetPackage: String,
        includeSubpackages: Boolean
    ): Boolean {
        return if (includeSubpackages) {
            classPackage == targetPackage || classPackage.startsWith("$targetPackage.")
        } else {
            classPackage == targetPackage
        }
    }

    private fun toDescriptor(fqcn: String): String = "L${fqcn.replace('.', '/')};"
}
