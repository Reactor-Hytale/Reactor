package codes.reactor.sdk.bundled.scanner

import codes.reactor.kernel.Reactor
import codes.reactor.kernel.plugin.scope.provider.DependencyProvider
import codes.reactor.kernel.plugin.scope.provider.ProtectedDependencyProvider
import codes.reactor.sdk.scanner.PackageScanner

class ScannerProvider(
    override val type: Class<PackageScanner> = PackageScanner::class.java,
) : DependencyProvider<PackageScanner>,
    ProtectedDependencyProvider {

    override fun provide(): PackageScanner {
        val snapshot = Reactor.pluginCatalog.getFromCurrentScope()
            ?: error("No plugin snapshot available")

        return DefaultPackageScanner(
            defaultPackage = snapshot.runtime.rootPackage,
            classLoader = Thread.currentThread().contextClassLoader,
            logger = Reactor.loggerFactory.createLogger(snapshot.metadata.id.value + " Scanner"),
        )
    }
}
