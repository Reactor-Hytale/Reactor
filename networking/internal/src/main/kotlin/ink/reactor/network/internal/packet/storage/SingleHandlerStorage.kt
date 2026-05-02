package codes.reactor.network.internal.packet.storage

import codes.reactor.network.api.packet.Packet
import codes.reactor.network.api.packet.handler.PacketHandler
import codes.reactor.network.api.player.PlayerConnection

internal class SingleHandlerStorage(private val handler: PacketHandler) : MutablePacketHandlerStorage {

    override fun callHandlers(connection: PlayerConnection, packet: Packet) {
        handler.handle(connection, packet)
    }

    override fun add(handler: PacketHandler): MutablePacketHandlerStorage {
        return ArrayHandlerStorage(arrayOf(this.handler, handler))
    }

    override fun remove(handler: PacketHandler): MutablePacketHandlerStorage? {
        return if (this.handler == handler) null else this
    }

    override fun add(vararg handlers: PacketHandler): MutablePacketHandlerStorage {
        val packetHandlers = handlers.toMutableList().apply { add(handler) }.toTypedArray()
        return ArrayHandlerStorage(packetHandlers)
    }

    override fun remove(vararg handlers: PacketHandler): MutablePacketHandlerStorage? {
        return if (handlers.isEmpty()) this else remove(handlers[0])
    }
}
