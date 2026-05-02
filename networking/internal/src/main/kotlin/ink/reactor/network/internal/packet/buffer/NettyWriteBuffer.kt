package codes.reactor.network.internal.packet.buffer

import codes.reactor.network.api.buffer.WriteBuffer
import io.netty.buffer.ByteBuf

class NettyWriteBuffer(
    private val buf: ByteBuf
): WriteBuffer {

    override fun writeBytes(bytes: ByteArray) {
        buf.writeBytes(bytes)
    }

    override fun writeChars(chars: CharArray) {
        for (c in chars) {
            buf.writeChar(c.code)
        }
    }

    override fun writeBoolean(condition: Boolean) {
        buf.writeByte(if (condition) 1 else 0)
    }

    override fun writeByte(value: Byte) {
        buf.writeByte(value.toInt())
    }

    override fun writeByte(value: Int) {
        buf.writeByte(value)
    }

    override fun writeShort(value: Int) {
        buf.writeShort(value)
    }

    override fun writeChar(character: Char) {
        buf.writeChar(character.code)
    }

    override fun writeInt(value: Int) {
        buf.writeInt(value)
    }

    override fun writeLong(value: Long) {
        buf.writeLong(value)
    }

    override fun writeFloat(value: Float) {
        buf.writeFloat(value)
    }

    override fun writeDouble(value: Double) {
        buf.writeDouble(value)
    }

    override fun writeString(string: String) {
        buf.writeBytes(string.toByteArray())
    }

    override fun writeLongArray(longs: LongArray) {
        for (long in longs) {
            buf.writeLong(long)
        }
    }

    override fun revert(amountBytes: Int) {
        buf.writerIndex(buf.writerIndex() - amountBytes)
    }

    override fun skip(amountBytes: Int) {
        buf.writerIndex(buf.writerIndex() + amountBytes)
    }

    override val index: Int
        get() = buf.writerIndex()
}
