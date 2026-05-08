package codes.reactor.network.internal.packet

import io.netty.buffer.ByteBuf

/**
 * A pre-serialized packet cache designed for efficient sending to multiple players.
 *
 * This class holds a packet that has already been serialized into a [ByteBuf],
 * allowing it to be sent to multiple [codes.reactor.network.api.player.PlayerConnection]s without re-encoding
 * the underlying [codes.reactor.network.api.packet.Packet] each time.
 *
 * The cache includes a time-to-live (TTL) counter. When the TTL reaches zero,
 * the internal [ByteBuf] is automatically released, preventing memory leaks.
 * This is particularly useful for packets that are broadcast repeatedly
 * (e.g., global notifications, world updates) but have a limited lifespan.
 *
 * @param serializedBuf The pre-serialized packet data.
 * @param ttl The number of remaining sends before the buffer is automatically released.
 */
class CachedPacket(
    private val serializedBuf: ByteBuf,
    private var ttl: Int
) {
    private val lock = Any()

    fun retainForWrite(): ByteBuf? = synchronized(lock) {
        if (ttl <= 0 || serializedBuf.refCnt() <= 0) return null
        return serializedBuf.duplicate().retain()
    }

    fun onCompleteWrite() = synchronized(lock) {
        if (ttl > 0) {
            ttl--
            if (ttl <= 0) {
                if (serializedBuf.refCnt() > 0) {
                    serializedBuf.release()
                }
            }
        }
    }
}
