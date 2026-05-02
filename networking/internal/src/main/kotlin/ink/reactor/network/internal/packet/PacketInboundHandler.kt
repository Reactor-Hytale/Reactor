package codes.reactor.network.internal.packet

import codes.reactor.network.api.NetworkConnector
import codes.reactor.network.api.packet.Packet
import codes.reactor.network.api.player.PlayerConnection
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class PacketInboundHandler(private val connection: PlayerConnection): SimpleChannelInboundHandler<Packet>() {
    override fun channelRead0(ctx: ChannelHandlerContext, packet: Packet) {
        NetworkConnector.packetHandlers.callHandlers(connection, packet)
    }
}
