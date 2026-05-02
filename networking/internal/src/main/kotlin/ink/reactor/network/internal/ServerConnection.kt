package codes.reactor.network.internal

import codes.reactor.kernel.Reactor
import codes.reactor.network.internal.config.NetworkConfig
import codes.reactor.network.internal.quic.QuicTransport
import io.netty.channel.EventLoopGroup
import io.netty.channel.IoHandlerFactory
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollIoHandler
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueIoHandler
import io.netty.channel.nio.NioIoHandler

internal class ServerConnection {

    private var transport: QuicTransport? = null

    fun init(config: NetworkConfig) {
        val quicTransport = QuicTransport(
            getEventLoop(config.threadCount),
            Reactor.loggerFactory.createLogger("QuicTransport"),
        )
        quicTransport.bind(config)

        transport = quicTransport
    }

    fun shutdown() {
        transport?.shutdown()
    }

    private fun getEventLoop(expectedThreads: Int): EventLoopGroup {
        val threads = if (expectedThreads <= 0) Runtime.getRuntime().availableProcessors() else expectedThreads

        val ioFactory: IoHandlerFactory = when {
            Epoll.isAvailable() -> EpollIoHandler.newFactory()
            KQueue.isAvailable() -> KQueueIoHandler.newFactory()
            else -> NioIoHandler.newFactory()
        }

        return MultiThreadIoEventLoopGroup(threads, ioFactory)
    }
}
