package codes.reactor.network.internal.player

import codes.reactor.kernel.logger.Logger
import codes.reactor.network.api.packet.Packet
import codes.reactor.network.api.player.PlayerConnection
import codes.reactor.network.internal.packet.CachedPacket
import io.netty.channel.Channel

class NettyPlayerConnection(
    val ctx: Channel,
    private val logger: Logger
) : PlayerConnection {

    override fun sendPacket(packet: Packet) {
        if (!isOnline()) return

        if (ctx.eventLoop().inEventLoop()) {
            ctx.writeAndFlush(packet)
        } else {
            ctx.eventLoop().execute { ctx.writeAndFlush(packet) }
        }
    }

    override fun sendPackets(vararg packets: Packet) {
        if (!isOnline()) return

        if (ctx.eventLoop().inEventLoop()) {
            for (packet in packets) {
                ctx.write(packet)
            }
            ctx.flush()
            return
        }

        ctx.eventLoop().execute {
            for (packet in packets) {
                ctx.write(packet)
            }
            ctx.flush()
        }
    }

    fun sendCachedPacket(packet: CachedPacket) {
        if (!isOnline()) {
            packet.onCompleteWrite()
            return
        }

        val buf = packet.retainForWrite()
        if (buf == null) {
            packet.onCompleteWrite()
            return
        }

        val writeTask = {
            ctx.writeAndFlush(buf).addListener { future ->
                try {
                    if (!future.isSuccess) {
                        logger.warn("Failed to send cached packet: ${future.cause()?.message}")
                    }
                } finally {
                    packet.onCompleteWrite()
                }
            }
            Unit
        }

        if (ctx.eventLoop().inEventLoop()) {
            writeTask()
        } else {
            ctx.eventLoop().execute(writeTask)
        }
    }

    override fun disconnect(reason: String) {
        ctx.close()
        logger.info("Disconnect: ${getIp()}. Reason: $reason")
    }

    override fun isOnline(): Boolean = ctx.isActive

    override fun getIp(): String = ctx.remoteAddress().toString()
}
