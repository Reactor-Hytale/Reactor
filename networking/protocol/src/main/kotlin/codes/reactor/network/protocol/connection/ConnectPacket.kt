package codes.reactor.network.protocol.connection

import codes.reactor.network.api.buffer.ReadBuffer
import codes.reactor.network.api.buffer.WriteBuffer
import codes.reactor.network.api.packet.Packet

import java.util.*

class ConnectPacket(
    var protocolHash: String = "",
    var clientType: ClientType = ClientType.GAME,
    var language: String? = null,
    var identityToken: String? = null,
    var uuid: UUID = UUID(0, 0),
    var userName: String = "",
    var referralData: Array<Byte>? = null,
    var host: String,
    var port: Short,
): Packet {

    enum class ClientType {
        GAME, EDITOR
    }

    override fun write(buf: WriteBuffer) {
        TODO("Not yet implemented")
    }

    override fun read(buf: ReadBuffer) {
        TODO("Not yet implemented")
    }

    override fun maxSize(): Int {
        TODO("Not yet implemented")
    }

    override fun size(): Int {
        TODO("Not yet implemented")
    }

    override fun id() = 0
}
