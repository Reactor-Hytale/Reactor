package codes.reactor.network.api.packet

import codes.reactor.network.api.buffer.ReadBuffer
import codes.reactor.network.api.buffer.WriteBuffer

interface Packet {
    fun write(buf: WriteBuffer)
    fun read(buf: ReadBuffer)

    fun maxSize(): Int
    fun size(): Int

    fun id(): Int

    fun needCompression(): Boolean = false
}
