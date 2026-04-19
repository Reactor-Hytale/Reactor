package ink.reactor.network.internal.packet

import io.netty.buffer.ByteBuf

/**
 * A pre-serialized packet cache designed for efficient sending to multiple players.
 *
 * This class holds a packet that has already been serialized into a [ByteBuf],
 * allowing it to be sent to multiple [ink.reactor.network.api.player.PlayerConnection]s without re-encoding
 * the underlying [ink.reactor.network.api.packet.Packet] each time.
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
    @Volatile private var ttl: Int
) {

    @Synchronized
    fun retainForWrite(): ByteBuf? {
        if (ttl <= 0 || serializedBuf.refCnt() <= 0) return null
        return serializedBuf.retainedDuplicate()
    }

    @Synchronized
    fun onCompleteWrite() {
        ttl--
        if (ttl <= 0 && serializedBuf.refCnt() > 0) {
            serializedBuf.release()
        }
    }
}
