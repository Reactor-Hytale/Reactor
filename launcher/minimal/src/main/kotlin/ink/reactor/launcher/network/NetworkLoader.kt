package ink.reactor.launcher.network

import ink.reactor.kernel.Reactor
import ink.reactor.kernel.logger.Logger
import ink.reactor.network.internal.NetworkInternalConnector
import ink.reactor.network.internal.config.NetworkConfig
import ink.reactor.network.internal.config.QuicConfig
import ink.reactor.sdk.config.ConfigService
import ink.reactor.sdk.util.TimeFormatter
import kotlin.math.max
import kotlin.math.min

class NetworkLoader(
    private val logger: Logger,
) {
    fun load(configService: ConfigService) {
        val config = configService.createIfAbsentAndLoad("network");
        val quicSection = config.getOrCreateSection("quic")

        val quicConfig = QuicConfig(
            quicSection.getInt("max-streams", 100).toLong(),
            quicSection.getInt("max-data", 1024 * 1024 * 10).toLong(),
        )

        val networkConfig = NetworkConfig(
            config.getOrDefault("host", "0.0.0.0"),
            config.getInt("port", 5520),
            config.getInt("threads-to-use", 0),
            min(22, max(-1, config.getInt("zstd-compression-level", 3))),
            TimeFormatter.parseToSeconds(config.getOrDefault("read-timeout", "10s")).toInt(),
            config.getBoolean("reuse-address"),
            quicConfig
        )

        NetworkInternalConnector.init(networkConfig)
        Reactor.addStopTask { NetworkInternalConnector.shutdown() }
        logger.info("Server networking started")
    }
}
