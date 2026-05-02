package codes.reactor.network.internal.io

import codes.reactor.kernel.logger.Logger
import codes.reactor.network.api.packet.PacketRegistry
import codes.reactor.network.internal.io.frame.PacketFramer
import codes.reactor.network.protocol.data.DataSize
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

private const val HEADER_LENGTH =
    DataSize.INT + // Payload
    DataSize.INT   // PacketId

private const val MAX_PAYLOAD_LENGTH = 0x64000000 // 160 MiB

class PacketDecoder(private val logger: Logger) : ByteToMessageDecoder() {

    override fun decode(
        ctx: ChannelHandlerContext,
        buf: ByteBuf,
        out: MutableList<Any>
    ) {
        if (buf.readableBytes() < HEADER_LENGTH) {
            return
        }

        buf.markReaderIndex()
        val payloadLength = buf.readIntLE()

        if (payloadLength < 0) {
            logger.warn("Received negative payload length: $payloadLength from ${ctx.channel().remoteAddress()}")
            ctx.close()
            return
        }

        if (payloadLength > MAX_PAYLOAD_LENGTH) {
            logger.warn("Payload length $payloadLength exceeds maximum $MAX_PAYLOAD_LENGTH from ${ctx.channel().remoteAddress()}")
            ctx.close()
            return
        }

        val packetId = buf.readIntLE()
        val packet = PacketRegistry.get(packetId)

        if (packet == null) {
            logger.warn("Unknown packet id: $packetId from ${ctx.channel().remoteAddress()}")
            ctx.close()
            return
        }

        val packetMaxSize = packet.maxSize()
        if (payloadLength > packetMaxSize) {
            logger.warn("Payload length $payloadLength exceeds packet max size $packetMaxSize for id $packetId from ${ctx.channel().remoteAddress()}")
            ctx.close()
            return
        }

        if (buf.readableBytes() < payloadLength) {
            buf.resetReaderIndex()
            return
        }

        PacketFramer.readFramedPacket(buf, packet, payloadLength)

        out.add(packet)
        logger.debug("Received packet framed: Size: ${packet.size()} id: $packetId")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error("Unhandled exception", cause)
        ctx.close()
    }
}
