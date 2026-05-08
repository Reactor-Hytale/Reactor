package codes.reactor.network.api.packet

import codes.reactor.network.api.player.PlayerConnection

/**
 * A high-performance packet dispatcher optimized for sending packets to multiple connections.
 *
 * This interface provides methods to send packets efficiently to single or multiple
 * [PlayerConnection]s. It is specifically designed to reuse packet serialization
 * when sending the same packet to multiple players, avoiding redundant encode operations.
 *
 * For sending a packet to a single player, [PlayerConnection.sendPacket] is a
 * convenience method. However, when broadcasting the same packet to multiple players,
 * using [PacketsSender] provides significantly better performance.
 */
interface PacketsSender {

    /**
     * Sends multiple packets to multiple player connections efficiently.
     *
     * This method optimizes serialization by reusing encoded packet data
     * across all specified connections.
     *
     * @param connections The collection of target player connections.
     * @param packets The packets to send.
     */
    fun sendPackets(connections: Collection<PlayerConnection>, vararg packets: Packet)

    /**
     * Sends a single packet to multiple player connections efficiently.
     *
     * This method optimizes performance by serializing the packet once
     * and reusing the encoded data for all specified connections.
     * For broadcasting the same packet to many players, this is the
     * recommended approach over individual [PlayerConnection.sendPacket] calls.
     *
     * @param connections The collection of target player connections.
     * @param packet The packet to send.
     */
    fun sendPacket(connections: Collection<PlayerConnection>, packet: Packet)
}
