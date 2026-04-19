package ink.reactor.network.api.buffer

import java.util.*

interface ReadBuffer {
    fun readBytes(length: Int): ByteArray
    fun readChars(length: Int): CharArray
    fun readLongArray(): LongArray
    fun readBoolean(): Boolean
    fun readByte(): Byte
    fun readUnsignedByte(): Int
    fun readShort(): Short
    fun readChar(): Char
    fun readInt(): Int
    fun readUUID(): UUID?
    fun readLong(): Long
    fun readFloat(): Float
    fun readDouble(): Double
    fun readString(): String

    fun skipTo(index: Int)

    val index: Int
}
