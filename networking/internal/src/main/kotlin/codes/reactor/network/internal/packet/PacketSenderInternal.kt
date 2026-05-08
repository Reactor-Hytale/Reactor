package codes.reactor.network.internal.packet

import codes.reactor.kernel.Reactor
import codes.reactor.kernel.logger.logger
import codes.reactor.network.api.packet.Packet
import codes.reactor.network.api.packet.PacketsSender
import codes.reactor.network.api.player.PlayerConnection
import codes.reactor.network.internal.io.PacketEncoder
import codes.reactor.network.internal.io.frame.PacketFramer
import codes.reactor.network.internal.player.NettyPlayerConnection

class PacketSenderInternal: PacketsSender {

    override fun sendPacket(
        connections: Collection<PlayerConnection>,
        packet: Packet
    ) {
        if (connections.isEmpty()) {
            return
        }

        val buf = PacketEncoder.allocateBuf(packet) ?: return
        val cachedPacket = CachedPacket(buf, connections.size)

        try {
            PacketFramer.writeFramedPacket(packet, buf)
        } catch (e: Exception) {
            repeat(connections.size) { cachedPacket.onCompleteWrite() }
            logger().error("Failed to write packet to buffer", e)
            return
        }

        var sentCount = 0
        for (connection in connections) {
            if (connection is NettyPlayerConnection) {
                connection.sendCachedPacket(cachedPacket)
                sentCount++
            }
        }

        // Release TTLs for connections that weren't NettyPlayerConnection
        val unsentCount = connections.size - sentCount
        repeat(unsentCount) { cachedPacket.onCompleteWrite() }
    }

    override fun sendPackets(
        connections: Collection<PlayerConnection>,
        vararg packets: Packet
    ) {
        if (connections.isEmpty() || packets.isEmpty()) {
            return
        }

        for (packet in packets) {
            sendPacket(connections, packet)
        }
    }
}
