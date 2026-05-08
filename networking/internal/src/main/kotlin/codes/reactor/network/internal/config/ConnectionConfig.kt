package codes.reactor.network.internal.config

class NetworkConfig(
    val host: String,
    val port: Int,

    val threadCount: Int,

    val zstdCompressionLevel: Int,
    val readTimeoutSeconds: Int,

    val reuseAddress: Boolean,

    val quicConfig: QuicConfig
)

class QuicConfig(
    val maxStreams: Long,
    val maxData: Long
)
