package codes.reactor.sdk.config

import java.util.concurrent.ConcurrentHashMap

/**
 * A thread-safe registry for [ConfigService] implementations.
 *
 * <p>Services are registered by all supported file extensions. Each extension is normalized
 * by removing a leading dot and converting it to lowercase.</p>
 */
object ConfigServiceRegistry {

    private val services = ConcurrentHashMap<String, ConfigService>()

    /**
     * Registers the given config service for all its supported extensions.
     *
     * <p>If a service is registered for an extension that already exists,
     * the previous mapping is replaced.</p>
     *
     * @param service the service to register
     * @throws IllegalArgumentException if the service declares no extensions
     */
    fun register(service: ConfigService) {
        require(service.fileExtensions.isNotEmpty()) {
            "ConfigService must declare at least one file extension."
        }

        service.fileExtensions.forEach { extension ->
            services[normalizeExtension(extension)] = service
        }
    }

    /**
     * Returns the config service associated with the given extension.
     *
     * <p>The extension may be provided with or without a leading dot and in any case.
     * For example, all the following are valid if registered:</p>
     *
     * <pre>{@code
     * ConfigServiceRegistry["yaml"]
     * ConfigServiceRegistry["YAML"]
     * ConfigServiceRegistry[".yaml"]
     * }</pre>
     *
     * @param extension the file extension to resolve
     * @return the matching config service
     * @throws IllegalArgumentException if no service is registered for the extension
     */
    operator fun get(extension: String): ConfigService {
        return services[normalizeExtension(extension)]
            ?: throw IllegalArgumentException("No config service registered for extension: $extension")
    }

    /**
     * Removes all registered services from the registry.
     *
     * <p>This is primarily useful for tests or controlled reinitialization.</p>
     */
    fun clear() {
        services.clear()
    }

    private fun normalizeExtension(extension: String): String {
        return extension.trim().removePrefix(".").lowercase()
    }
}
