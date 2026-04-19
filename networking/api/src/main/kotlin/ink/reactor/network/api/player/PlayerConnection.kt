package ink.reactor.network.api.player

import ink.reactor.network.api.packet.Packet

/**
 * Represents a network connection to a player.
 */
interface PlayerConnection {
    /**
     * Sends a packet to this specific player connection.
     *
     * This is a convenience method for simple packet sending to a single player.
     * For sending the same packet to multiple players, consider using
     * [ink.reactor.network.api.packet.PacketsSender] for better performance.
     *
     * @param packet The packet to send.
     */
    fun sendPacket(packet: Packet)

    /**
     * Sends multiple packets to this specific player connection.
     *
     * This is a convenience method for simple packet sending to a single player.
     * For sending the same packets to multiple players, consider using
     * [ink.reactor.network.api.packet.PacketsSender] for better performance.
     *
     * @param packets The packets to send.
     */
    fun sendPackets(vararg packets: Packet)

    /**
     * Disconnects the player with the specified reason.
     *
     * @param reason The disconnection reason displayed to the player.
     */
    fun disconnect(reason: String)

    fun getIp(): String

    fun isOnline(): Boolean
}
