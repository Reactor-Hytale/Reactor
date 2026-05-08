package codes.reactor.network.api.packet.handler

import codes.reactor.network.api.packet.Packet
import codes.reactor.network.api.player.PlayerConnection

interface PacketHandlerStorage {
    fun callHandlers(connection: PlayerConnection, packet: Packet)

    fun add(handler: PacketHandler)
    fun remove(handler: PacketHandler)

    fun add(vararg handlers: PacketHandler)
    fun remove(vararg handlers: PacketHandler)

    fun clear(id: Int)
}
