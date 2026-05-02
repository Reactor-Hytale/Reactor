package codes.reactor.network.internal.packet.storage

import codes.reactor.network.api.packet.Packet
import codes.reactor.network.api.packet.handler.PacketHandler
import codes.reactor.network.api.packet.handler.PacketHandlerStorage
import codes.reactor.network.api.player.PlayerConnection

class StatePacketHandlerStorage(size: Int) : PacketHandlerStorage {

    private val packetHandlerStorage = arrayOfNulls<MutablePacketHandlerStorage>(size)

    override fun callHandlers(connection: PlayerConnection, packet: Packet) {
        val packetID = packet.id()
        requirePacketIdInRange(packetID)
        packetHandlerStorage[packetID]?.callHandlers(connection, packet)
    }

    override fun add(handler: PacketHandler) {
        val packetID = handler.packetId()
        requirePacketIdInRange(packetID)

        val currentStorage = packetHandlerStorage[packetID]
        packetHandlerStorage[packetID] = currentStorage?.add(handler) ?: SingleHandlerStorage(handler)
    }

    override fun remove(handler: PacketHandler) {
        val packetID = handler.packetId()
        requirePacketIdInRange(packetID)

        packetHandlerStorage[packetID] = packetHandlerStorage[packetID]?.remove(handler)
    }

    override fun add(vararg handlers: PacketHandler) {
        for (handler in handlers) {
            add(handler)
        }
    }

    override fun remove(vararg handlers: PacketHandler) {
        for (handler in handlers) {
            remove(handler)
        }
    }

    override fun clear(id: Int) {
        packetHandlerStorage[id] = null
    }

    private fun requirePacketIdInRange(packetID: Int) {
        if (packetID < 0 || packetID >= packetHandlerStorage.size) {
            throw ArrayIndexOutOfBoundsException(
                "Packet id needs to be in the range 0-${packetHandlerStorage.size - 1} but found $packetID"
            )
        }
    }
}
