package ink.reactor.network.api.packet

import ink.reactor.network.api.buffer.ReadBuffer
import ink.reactor.network.api.buffer.WriteBuffer

interface Packet {
    fun write(buf: WriteBuffer)
    fun read(buf: ReadBuffer)

    fun maxSize(): Int
    fun size(): Int

    fun id(): Int

    fun needCompression(): Boolean = false
}
