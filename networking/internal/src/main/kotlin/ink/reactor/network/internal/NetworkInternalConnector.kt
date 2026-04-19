package ink.reactor.network.internal

import ink.reactor.network.api.NetworkConnector
import ink.reactor.network.api.packet.PacketRegistry
import ink.reactor.network.internal.config.NetworkConfig
import ink.reactor.network.internal.packet.PacketSenderInternal
import ink.reactor.network.internal.packet.storage.StatePacketHandlerStorage

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
