package codes.reactor.network.api.buffer

interface WriteBuffer {
    fun writeBytes(bytes: ByteArray)
    fun writeChars(chars: CharArray)
    fun writeBoolean(condition: Boolean)
    fun writeByte(value: Byte)
    fun writeByte(value: Int)
    fun writeShort(value: Int)
    fun writeChar(character: Char)
    fun writeInt(value: Int)
    fun writeLong(value: Long)
    fun writeFloat(value: Float)
    fun writeDouble(value: Double)
    fun writeString(string: String)
    fun writeLongArray(longs: LongArray)

    fun revert(amountBytes: Int)
    fun skip(amountBytes: Int)

    val index: Int
}
