package codes.reactor.network.internal.packet.storage

import codes.reactor.network.api.packet.Packet
import codes.reactor.network.api.packet.handler.PacketHandler
import codes.reactor.network.api.player.PlayerConnection

internal interface MutablePacketHandlerStorage {
    fun callHandlers(connection: PlayerConnection, packet: Packet)

    fun add(handler: PacketHandler): MutablePacketHandlerStorage
    fun remove(handler: PacketHandler): MutablePacketHandlerStorage?

    fun add(vararg handlers: PacketHandler): MutablePacketHandlerStorage
    fun remove(vararg handlers: PacketHandler): MutablePacketHandlerStorage?
}
