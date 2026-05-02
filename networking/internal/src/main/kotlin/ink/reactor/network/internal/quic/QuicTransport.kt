package codes.reactor.network.internal.quic

import codes.reactor.kernel.Reactor
import codes.reactor.kernel.logger.Logger
import codes.reactor.network.internal.config.NetworkConfig
import codes.reactor.network.internal.quic.certificate.RuntimeCertificateProvider
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollDatagramChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueDatagramChannel
import io.netty.channel.socket.DatagramChannel
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.handler.ssl.ClientAuth
import io.netty.incubator.codec.quic.QuicSslContext
import io.netty.incubator.codec.quic.QuicSslContextBuilder
import io.netty.util.AttributeKey
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.file.Path
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory

class QuicTransport(
    private val workerGroup: EventLoopGroup,
    private val logger: Logger
) {

    companion object {
        val CLIENT_CERTIFICATE_ATTR: AttributeKey<X509Certificate> = AttributeKey.valueOf<X509Certificate>("CLIENT_CERTIFICATE")
    }

    private val activeChannels = mutableListOf<Channel>()
    private val sslContext: QuicSslContext

    init {
        val certPath = Path.of("certs/server.p12")
        val ks = RuntimeCertificateProvider.loadOrCreate(certPath)
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())

        kmf.init(ks, RuntimeCertificateProvider.PASSWORD)

        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)

        sslContext = QuicSslContextBuilder.forServer(kmf, null)
            .applicationProtocols("hytale/1")
            .earlyData(false)
            .clientAuth(ClientAuth.REQUIRE)
            .trustManager(tmf)
            .build()
    }

    fun bind(config: NetworkConfig) {
        val bootstrap = Bootstrap()
            .group(workerGroup)
            .channelFactory(getDatagramChannelFactory())
            .option(ChannelOption.SO_REUSEADDR, config.reuseAddress)
            .handler(object : ChannelInitializer<DatagramChannel>() {
                override fun initChannel(ch: DatagramChannel) {
                    ch.pipeline().addLast(QuicDatagramHandler(sslContext, logger))
                }
            })

        val host = config.host
        val port = config.port

        val bindAll = host == "0.0.0.0" || host == "::0" || host.isEmpty()

        if (!bindAll) {
            try {
                val future = bootstrap.bind(InetSocketAddress(host, port)).sync()
                if (future.isSuccess) {
                    activeChannels.add(future.channel())
                    logger.info("QUIC listening on specific host: $host:$port")
                }
            } catch (e: Exception) {
                throw IllegalStateException("Failed to bind to specific host $host:$port", e)
            }
            return
        }

        try {
            val v4Future = bootstrap.bind(InetSocketAddress("0.0.0.0", port)).sync()
            if (v4Future.isSuccess) {
                activeChannels.add(v4Future.channel())
                logger.info("listening on IPv4: 0.0.0.0:$port")
            }
        } catch (e: Exception) {
            logger.warn("Could not bind IPv4: ${e.message}")
        }

        try {
            val v6Future = bootstrap.bind(InetSocketAddress("::0", port)).sync()
            if (v6Future.isSuccess) {
                activeChannels.add(v6Future.channel())
                logger.info("listening on IPv6: [::]:$port")
            }
        } catch (e: Exception) {
            logger.warn("Could not bind IPv6 (might be covered by dual-stack OS): ${e.message}")
        }

        if (activeChannels.isEmpty()) {
            error("Failed to bind on any protocol (IPv4 or IPv6) on port $port")
        }
    }

    fun shutdown() {
        logger.info("Shutting down")
        activeChannels.forEach { it.close().awaitUninterruptibly() }
        activeChannels.clear()

        try {
            workerGroup.shutdownGracefully(0L, 1L, TimeUnit.SECONDS).await(1L, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            logger.error("Failed to shutdown gracefully.", e)
        }
    }

    private fun getDatagramChannelFactory(): ChannelFactory<out DatagramChannel> {
        return when {
            Epoll.isAvailable() -> ReflectiveChannelFactory(EpollDatagramChannel::class.java)
            KQueue.isAvailable() -> ReflectiveChannelFactory(KQueueDatagramChannel::class.java)
            else -> ReflectiveChannelFactory(NioDatagramChannel::class.java)
        }
    }
}
