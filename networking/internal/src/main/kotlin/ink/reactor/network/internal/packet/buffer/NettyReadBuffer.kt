package ink.reactor.network.internal.packet.buffer

import ink.reactor.network.api.buffer.ReadBuffer
import io.netty.buffer.ByteBuf
import java.util.UUID

class NettyReadBuffer(
    private val buffer: ByteBuf,
): ReadBuffer {

    override fun readBytes(length: Int): ByteArray {
        return ByteArray(length).also { buffer.readBytes(it) }
    }

    override fun readChars(length: Int): CharArray {
        return CharArray(length).also { buffer.readCharSequence(length, Charsets.UTF_8).toString().toCharArray(it, 0, length) }
    }

    override fun readLongArray(): LongArray {
        val length = buffer.readInt()
        return LongArray(length).also { for (i in 0 until length) it[i] = buffer.readLong() }
    }

    override fun readBoolean(): Boolean {
        return buffer.readBoolean()
    }

    override fun readByte(): Byte {
        return buffer.readByte()
    }

    override fun readUnsignedByte(): Int {
        return buffer.readUnsignedByte().toInt()
    }

    override fun readShort(): Short {
        return buffer.readShort()
    }

    override fun readChar(): Char {
        return buffer.readChar()
    }

    override fun readInt(): Int {
        return buffer.readInt()
    }

    override fun readUUID(): UUID {
        val mostSigBits = buffer.readLong()
        val leastSigBits = buffer.readLong()
        return UUID(mostSigBits, leastSigBits)
    }

    override fun readLong(): Long {
        return buffer.readLong()
    }

    override fun readFloat(): Float {
        return buffer.readFloat()
    }

    override fun readDouble(): Double {
        return buffer.readDouble()
    }

    override fun readString(): String {
        val length = buffer.readInt()
        return buffer.readCharSequence(length, Charsets.UTF_8).toString()
    }

    override fun skipTo(index: Int) {
        buffer.readerIndex(index)
    }

    override val index: Int
        get() = buffer.readerIndex()
}
