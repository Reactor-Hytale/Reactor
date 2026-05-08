package codes.reactor.network.internal

import codes.reactor.network.api.NetworkConnector
import codes.reactor.network.api.packet.PacketRegistry
import codes.reactor.network.internal.config.NetworkConfig
import codes.reactor.network.internal.packet.PacketSenderInternal
import codes.reactor.network.internal.packet.storage.StatePacketHandlerStorage

class NetworkInternalConnector private constructor(
    private val config: NetworkConfig,
    private val connection: ServerConnection,
) {
    companion object {

        private var ref: NetworkInternalConnector? = null

        val config: NetworkConfig get() = connector.config

        fun init(
            config: NetworkConfig,
        ) {
            if (ref != null) {
                error("Network internal connector already initialized")
            }

            val connection = ServerConnection()
            ref = NetworkInternalConnector(config, connection)
            NetworkConnector.init(
                StatePacketHandlerStorage(PacketRegistry.MAX_PACKETS),
                PacketSenderInternal()
            )

            connection.init(config)
        }

        fun shutdown() {
            connector.connection.shutdown()
        }

        private val connector: NetworkInternalConnector
            get() = ref ?: error("Network internal connector not initialized")
    }
}
