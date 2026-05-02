package codes.reactor.gradle.scanner.asm

import codes.reactor.gradle.model.DependencyAnnotationData
import codes.reactor.gradle.model.PluginAnnotationData
import codes.reactor.gradle.model.ScannedClassInfo
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

internal class PluginClassAnnotationVisitor(
    private val pluginDescriptor: String,
    private val bootstrapDescriptors: Set<String>
) : ClassVisitor(Opcodes.ASM9) {

    private var className: String = ""
    private var packageName: String = ""
    private var hasPlugin: Boolean = false
    private var hasBootstrap: Boolean = false
    private var pluginData: PluginAnnotationData? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name.replace('/', '.')
        packageName = className.substringBeforeLast('.', "")
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        if (descriptor == pluginDescriptor) {
            hasPlugin = true
            val data = PluginAnnotationData()
            pluginData = data
            return PluginAnnotationReader(data)
        }

        if (descriptor in bootstrapDescriptors) {
            hasBootstrap = true
        }

        return null
    }

    fun toScannedClassInfo(): ScannedClassInfo {
        return ScannedClassInfo(className, packageName, hasPlugin, hasBootstrap, pluginData)
    }

    private class PluginAnnotationReader(
        private val target: PluginAnnotationData
    ) : AnnotationVisitor(Opcodes.ASM9) {

        override fun visit(name: String, value: Any) {
            when (name) {
                "id" -> target.id = value.toString()
                "version" -> target.version = value.toString()
                "description" -> target.description = value.toString()
            }
        }

        override fun visitArray(name: String): AnnotationVisitor? {
            return when (name) {
                "authors" -> AuthorsArrayReader(target)
                "dependencies" -> DependenciesArrayReader(target)
                "softDependencies" -> SoftDependenciesArrayReader(target)
                else -> null
            }
        }
    }

    private class AuthorsArrayReader(
        private val target: PluginAnnotationData
    ) : AnnotationVisitor(Opcodes.ASM9) {
        override fun visit(name: String?, value: Any) {
            target.authors += value.toString()
        }
    }

    private class DependenciesArrayReader(
        private val target: PluginAnnotationData
    ) : AnnotationVisitor(Opcodes.ASM9) {
        override fun visitAnnotation(name: String?, descriptor: String): AnnotationVisitor {
            val dependency = DependencyAnnotationData()
            target.dependencies += dependency
            return DependencyAnnotationReader(dependency)
        }
    }

    private class SoftDependenciesArrayReader(
        private val target: PluginAnnotationData
    ) : AnnotationVisitor(Opcodes.ASM9) {
        override fun visitAnnotation(name: String?, descriptor: String): AnnotationVisitor {
            val dependency = DependencyAnnotationData()
            target.softDependencies += dependency
            return DependencyAnnotationReader(dependency)
        }
    }

    private class DependencyAnnotationReader(
        private val target: DependencyAnnotationData
    ) : AnnotationVisitor(Opcodes.ASM9) {
        override fun visit(name: String, value: Any) {
            when (name) {
                "id" -> target.id = value.toString()
                "version" -> target.version = value.toString()
            }
        }
    }
}
