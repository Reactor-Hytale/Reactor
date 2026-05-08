package codes.reactor.network.api

import codes.reactor.network.api.packet.PacketsSender
import codes.reactor.network.api.packet.handler.PacketHandlerStorage

class NetworkConnector private constructor(
    private val packetHandlers: PacketHandlerStorage,
    private val packetsSender: PacketsSender
) {
    companion object {

        private var ref: NetworkConnector? = null

        val packetHandlers: PacketHandlerStorage get() = connector.packetHandlers
        val packetsSender: PacketsSender get() = connector.packetsSender

        fun init(
            packetHandlers: PacketHandlerStorage,
            packetsSender: PacketsSender,
        ) {
            if (ref != null) {
                error("Network connector already initialized")
            }
            ref = NetworkConnector(packetHandlers, packetsSender)
        }

        private val connector: NetworkConnector
            get() = ref ?: error("Network connector not initialized")
    }
}
