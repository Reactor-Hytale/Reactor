package codes.reactor.sdk.bundled.scanner.injector

import java.lang.reflect.Constructor
import java.util.*
import java.util.function.ToIntFunction

/**
 * Lightweight constructor-based dependency injector.
 *
 * This class allows you to register dependencies and automatically
 * instantiate classes by injecting the required constructor arguments.
 */
class DependencyInjector {

    /**
     * Registered dependency instances indexed by their associated type.
     */
    private val dependencies: MutableMap<Class<*>, Any> = HashMap()

    /**
     * Registers an instance using only its concrete runtime class.
     *
     * Example:
     * ```
     * injector.register(ClanStorageImpl())
     * ```
     *
     * This registers:
     * ```
     * ClanStorageImpl -> instance
     * ```
     *
     * @param instance instance to register
     * @param T instance type
     */
    fun <T : Any> register(instance: T) {
        dependencies[instance.javaClass] = instance
    }

    /**
     * Registers multiple instances.
     *
     * Each instance is registered in the same way as [register].
     *
     * Example:
     * ```
     * injector.register(
     *     ClanService(),
     *     PlayerRepository(),
     *     CacheManager()
     * )
     * ```
     *
     * @param instances instances to register
     */
    fun register(vararg instances: Any) {
        instances.forEach { instance -> register(instance) }
    }

    /**
     * Registers an explicit type-to-instance mapping.
     *
     * Example:
     * ```
     * injector.register(ClanStorage::class.java, impl)
     * ```
     *
     * This registers:
     * ```
     * ClanStorage -> impl
     * ```
     *
     * @param type target type used for lookup
     * @param instance instance to associate with the type
     * @param T instance type
     */
    fun <T : Any> register(type: Class<in T>, instance: T) {
        dependencies[type] = instance
    }

    /**
     * Registers an instance using its concrete class, implemented interfaces,
     * and superclass hierarchy.
     *
     * Example:
     * ```
     * ClanStorageImpl -> instance
     * ClanStorage     -> instance
     * Storage         -> instance
     * ```
     *
     * [Object] is intentionally ignored.
     *
     * @param instance instance to register
     * @param T instance type
     */
    fun <T : Any> registerWithHierarchy(instance: T) {
        val root: Class<*> = instance.javaClass

        dependencies[root] = instance

        registerInterfaces(root, instance)
        registerSuperClasses(root, instance)
    }

    /**
     * Recursively registers all interfaces implemented by the given type.
     *
     * @param type source type
     * @param instance instance associated with the hierarchy
     */
    private fun registerInterfaces(
        type: Class<*>,
        instance: Any
    ) {
        for (extendsInterface in type.interfaces) {
            dependencies.putIfAbsent(extendsInterface, instance)

            // Recursive interface hierarchy
            registerInterfaces(extendsInterface, instance)
        }
    }

    /**
     * Registers the superclass hierarchy of the given type.
     *
     * Interfaces implemented by each superclass are also registered.
     *
     * @param type source type
     * @param instance instance associated with the hierarchy
     */
    private fun registerSuperClasses(
        type: Class<*>,
        instance: Any
    ) {
        var current: Class<*>? = type.superclass

        while (current != null && current != Any::class.java && current != Object::class.java) {
            dependencies.putIfAbsent(current, instance)
            registerInterfaces(current, instance)
            current = current.superclass
        }
    }

    /**
     * Retrieves a registered dependency by its exact type.
     *
     * @param type dependency type
     * @param T dependency type
     * @return registered dependency instance, or `null` if not found
     */
    fun <T> get(type: Class<T>): T? {
        val dependency = dependencies[type] ?: return null
        return type.cast(dependency)
    }

    /**
     * Creates a new instance using constructor injection.
     *
     * ### Resolution rules
     * - Single constructor classes are recommended
     * - The largest satisfiable constructor is selected
     * - Dependency resolution uses exact type lookup only
     *
     * Constructors are evaluated in descending order by parameter count.
     * The first constructor whose parameters can all be resolved is used.
     *
     * @param clazz target class to instantiate
     * @param T target type
     * @return created instance
     * @throws IllegalStateException if no satisfiable constructor exists
     * @throws Exception if constructor invocation fails
     */
    @Throws(Exception::class)
    fun <T> createInstance(clazz: Class<T>): T {
        val constructors = clazz.declaredConstructors

        Arrays.sort(
            constructors,
            Comparator.comparingInt(
                ToIntFunction { c: Constructor<*> -> c.parameterCount }
            ).reversed()
        )

        for (constructor in constructors) {
            val args = resolveConstructorArguments(constructor) ?: continue

            constructor.isAccessible = true

            return clazz.cast(
                constructor.newInstance(*args)
            )
        }

        throw IllegalStateException("No satisfiable constructor found for ${clazz.name}")
    }

    /**
     * Resolves constructor arguments using registered dependencies.
     *
     * If at least one parameter cannot be resolved, `null` is returned.
     *
     * @param constructor constructor to resolve
     * @return resolved argument array, or `null` if resolution fails
     */
    private fun resolveConstructorArguments(
        constructor: Constructor<*>
    ): Array<Any?>? {
        val parameterTypes = constructor.parameterTypes
        val args = arrayOfNulls<Any>(parameterTypes.size)

        for (i in parameterTypes.indices) {
            val dependency = dependencies[parameterTypes[i]] ?: return null
            args[i] = dependency
        }

        return args
    }
}
