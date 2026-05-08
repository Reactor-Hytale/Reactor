package codes.reactor.network.api.packet.handler

import codes.reactor.network.api.packet.Packet
import codes.reactor.network.api.player.PlayerConnection

interface PacketHandler {
    fun handle(connection: PlayerConnection, packet: Packet)
    fun packetId(): Int
}
