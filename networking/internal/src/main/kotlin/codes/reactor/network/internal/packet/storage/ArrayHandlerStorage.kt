package codes.reactor.network.internal.packet.storage

import codes.reactor.network.api.packet.Packet
import codes.reactor.network.api.packet.handler.PacketHandler
import codes.reactor.network.api.player.PlayerConnection

internal class ArrayHandlerStorage(private var handlers: Array<PacketHandler>) : MutablePacketHandlerStorage {

    override fun callHandlers(connection: PlayerConnection, packet: Packet) {
        for (handler in handlers) {
            handler.handle(connection, packet)
        }
    }

    override fun add(handler: PacketHandler): MutablePacketHandlerStorage {
        handlers = handlers.plus(handler)
        return this
    }

    override fun remove(handler: PacketHandler): MutablePacketHandlerStorage? {
        val index = handlers.indexOf(handler)
        if (index == -1) return this

        return when (handlers.size) {
            1 -> null
            2 -> SingleHandlerStorage(handlers[if (index == 0) 1 else 0])
            else -> {
                val newHandlers = handlers.toMutableList().apply { removeAt(index) }.toTypedArray()
                this.handlers = newHandlers
                this
            }
        }
    }

    override fun add(vararg handlers: PacketHandler): MutablePacketHandlerStorage {
        this.handlers = this.handlers.plus(handlers)
        return this
    }

    override fun remove(vararg handlers: PacketHandler): MutablePacketHandlerStorage? {
        var storage: MutablePacketHandlerStorage? = this
        for (handler in handlers) {
            storage = storage?.remove(handler)
        }
        return storage
    }
}
