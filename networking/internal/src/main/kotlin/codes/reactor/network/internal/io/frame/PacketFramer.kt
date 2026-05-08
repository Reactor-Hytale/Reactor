package codes.reactor.network.internal.io.frame

import com.github.luben.zstd.Zstd
import codes.reactor.network.api.packet.Packet
import codes.reactor.network.internal.io.compression.PacketCompression
import codes.reactor.network.internal.io.compression.ProtocolException
import codes.reactor.network.internal.packet.buffer.NettyReadBuffer
import codes.reactor.network.internal.packet.buffer.NettyWriteBuffer
import codes.reactor.network.protocol.data.DataSize
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

object PacketFramer {

    private const val PAYLOAD_FIELD_SIZE = DataSize.INT
    private const val DEFAULT_INITIAL_CAPACITY = 256

    fun writeFramedPacket(
        packet: Packet,
        out: ByteBuf,
    ) {
        val lengthIndex = out.writerIndex()

        out.writeZero(PAYLOAD_FIELD_SIZE)
        out.writeIntLE(packet.id())

        val payloadStart = out.writerIndex()

        if (!packet.needCompression()) {
            packet.write(NettyWriteBuffer(out))

            val payloadSize = out.writerIndex() - payloadStart
            if (payloadSize > packet.maxSize()) {
                out.writerIndex(lengthIndex)
                throw ProtocolException("Packet too large: $payloadSize > ${packet.maxSize()}")
            }
            out.setIntLE(lengthIndex, payloadSize)
            return
        }

        val initialCapacity = if (packet.size() > 0) packet.size() else DEFAULT_INITIAL_CAPACITY
        val payloadBuf = out.alloc().buffer(initialCapacity)

        try {
            packet.write(NettyWriteBuffer(payloadBuf))
            val uncompressedSize = payloadBuf.readableBytes()

            if (uncompressedSize > packet.maxSize()) {
                throw ProtocolException("Packet too large before compression: $uncompressedSize")
            }

            val maxCompressedSize = Zstd.compressBound(uncompressedSize.toLong()).toInt()

            out.ensureWritable(maxCompressedSize)

            val compressedSize = PacketCompression.compress(
                payloadBuf,
                out,
                payloadStart,
                maxCompressedSize
            )

            out.writerIndex(payloadStart + compressedSize)
            out.setIntLE(lengthIndex, compressedSize)
        } finally {
            payloadBuf.release()
        }
    }

    fun readFramedPacket(
        buf: ByteBuf,
        packet: Packet,
        payloadLength: Int,
    ) {
        var payload: ByteBuf? = null

        try {
            if (packet.needCompression() && payloadLength > 0) {
                payload = PacketCompression.decompress(
                    buf.alloc(),
                    buf,
                    buf.readerIndex(),
                    payloadLength,
                    packet.maxSize()
                )
                buf.skipBytes(payloadLength)
            } else if (payloadLength > 0) {
                payload = buf.readRetainedSlice(payloadLength)
            } else {
                payload = Unpooled.EMPTY_BUFFER
            }

            packet.read(NettyReadBuffer(payload))

        } catch (e: Exception) {
            if (payload == null && buf.readableBytes() >= payloadLength) {
                 buf.skipBytes(payloadLength)
            }
            throw e
        } finally {
            if (payload != null && payload.refCnt() > 0 && payload != Unpooled.EMPTY_BUFFER) {
                payload.release()
            }
        }
    }
}
