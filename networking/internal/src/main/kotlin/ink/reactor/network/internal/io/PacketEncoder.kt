package ink.reactor.network.internal.io

import ink.reactor.network.api.packet.Packet
import ink.reactor.network.internal.io.frame.PacketFramer
import ink.reactor.network.protocol.data.DataSize
import io.netty.buffer.ByteBuf
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder

@ChannelHandler.Sharable
class PacketEncoder: MessageToMessageEncoder<Packet>() {

    companion object {
        fun estimatePacketSize(packet: Packet) = DataSize.INT + DataSize.INT +
            if (packet.needCompression())
                packet.maxSize()
            else
                packet.size()

        fun allocateBuf(size: Int): ByteBuf? = PooledByteBufAllocator.DEFAULT.buffer(size)
        fun allocateBuf(packet: Packet): ByteBuf? = PooledByteBufAllocator.DEFAULT.buffer(estimatePacketSize(packet))
        fun allocateBuf(packet: Packet, ctx: ChannelHandlerContext): ByteBuf? = ctx.alloc().buffer(estimatePacketSize(packet))
    }

    override fun encode(
        ctx: ChannelHandlerContext,
        packet: Packet,
        out: MutableList<Any>
    ) {
        val buf = allocateBuf(packet, ctx)
        if (buf != null) {
            PacketFramer.writeFramedPacket(packet, buf)
            out.add(buf)
        }
    }
}
