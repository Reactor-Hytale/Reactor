package codes.reactor.sdk.scanner

import codes.reactor.sdk.scanner.registrar.ListenerRegistrar
import codes.reactor.sdk.scanner.registrar.ObjectRegistrar

/**
 * High-level package scanning API used to discover and register runtime objects.
 *
 * This scanner is designed to reduce repetitive manual registration while
 * keeping object creation explicit and predictable.
 *
 * ## Object creation
 *
 * Classes discovered during scanning are instantiated using constructor injection.
 *
 * Constructor parameters are resolved exclusively from the provided instances
 * registered through:
 * - [provide]
 * - [provideAs]
 * - [provideWithHierarchy]
 *
 * ## Constructor resolution rules
 *
 * - Single constructor classes are recommended
 * - Constructors are evaluated in descending order by parameter count
 * - The largest satisfiable constructor is selected
 * - Dependency resolution uses exact type lookup only
 * - No field injection is performed
 * - No lifecycle ownership is handled by the scanner
 *
 * Example:
 * ```kotlin
 * class ClanListener(
 *     private val clanService: ClanService,
 *     private val logger: Logger
 * )
 * ```
 *
 * If both `ClanService` and `Logger` were previously provided,
 * the scanner will automatically instantiate the class.
 *
 */
interface PackageScanner {

    val defaultPackage: String

    /**
     * Registers an instance using only its concrete runtime type.
     *
     * Example:
     * ```kotlin
     * scanner.provide(ClanStorageImpl())
     * ```
     *
     * This registers:
     * ```text
     * ClanStorageImpl -> instance
     * ```
     *
     * @param instance instance to register
     * @param T instance type
     */
    fun <T : Any> provide(instance: T)

    /**
     * Registers multiple instances.
     *
     * Each instance is registered in the same way as [provide].
     *
     * Example:
     * ```kotlin
     * scanner.provide(
     *     ClanService(),
     *     PlayerRepository(),
     *     RedisClanStorage()
     * )
     * ```
     *
     * @param instances instances to register
     */
    fun provide(vararg instances: Any) {
        instances.forEach { instance -> provide(instance) }
    }

    /**
     * Registers an explicit type-to-instance mapping.
     *
     * Example:
     * ```kotlin
     * scanner.provideAs(ClanStorage::class.java, impl)
     * ```
     *
     * This registers:
     * ```text
     * ClanStorage -> impl
     * ```
     *
     * @param type lookup type
     * @param instance instance associated with the type
     * @param T instance type
     */
    fun <T : Any> provideAs(type: Class<in T>, instance: T)

    /**
     * Registers an instance using:
     * - its concrete type
     * - implemented interfaces
     * - superclass hierarchy
     *
     * Example:
     * ```text
     * ClanStorageImpl -> instance
     * ClanStorage     -> instance
     * Storage         -> instance
     * ```
     *
     * `Object` is intentionally ignored.
     *
     * @param instance instance to register
     * @param T instance type
     */
    fun <T : Any> provideWithHierarchy(instance: T)

    /**
     * Registers listener packages using the built-in [ListenerRegistrar].
     *
     * All discovered classes will be instantiated using constructor injection
     * and automatically subscribed to the runtime event bus.
     *
     * ## Default package behavior
     *
     * If no packages are specified, the scanner will automatically use:
     *
     * ```text
     * <plugin-root-package>.listeners
     * ```
     *
     * Example:
     * ```text
     * com.example.plugin.listeners
     * ```
     *
     * This allows simple projects to avoid explicitly declaring listener packages.
     *
     * ## Examples
     *
     * Explicit package:
     * ```kotlin
     * scanner.listeners(
     *     "com.example.plugin.listener"
     * )
     * ```
     *
     * Default inferred package:
     * ```kotlin
     * scanner.listeners()
     * ```
     *
     * @param packages packages to scan
     */
    fun listeners(vararg packages: String) {
        if (packages.isEmpty()) {
            registrar(ListenerRegistrar, "$defaultPackage.listeners")
            return
        }

        registrar(ListenerRegistrar, *packages)
    }
    /**
     * Registers a custom registrar for the specified packages.
     *
     * Registrars define how discovered instances are processed once created.
     *
     * Example:
     * ```kotlin
     * scanner.registrar(
     *     CommandRegistrar,
     *     "com.example.plugin.command"
     * )
     * ```
     *
     * @param instance registrar implementation
     * @param packages packages to scan
     */
    fun registrar(instance: ObjectRegistrar<*>, vararg packages: String)

    /**
     * Starts package scanning and object registration.
     *
     * All discovered classes are instantiated using constructor injection
     * and passed to their associated registrar.
     *
     * @return scan result information
     */
    fun scan(): ScanResult
}
