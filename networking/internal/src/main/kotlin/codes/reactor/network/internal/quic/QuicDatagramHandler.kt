package codes.reactor.network.internal.quic

import codes.reactor.kernel.logger.Logger
import codes.reactor.network.internal.NetworkInternalConnector
import codes.reactor.network.internal.player.PlayerConnectionInitializer
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.incubator.codec.quic.*
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

internal class QuicDatagramHandler(
    private val sslContext: QuicSslContext,
    private val logger: Logger
): ChannelInboundHandlerAdapter() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)

        val quicCodec = QuicServerCodecBuilder()
            .sslContext(sslContext)
            .tokenHandler(InsecureQuicTokenHandler.INSTANCE)
            .maxIdleTimeout(NetworkInternalConnector.config.readTimeoutSeconds.toLong(), TimeUnit.SECONDS)
            .ackDelayExponent(3)
            .initialMaxData(NetworkInternalConnector.config.quicConfig.maxData)
            .initialMaxStreamDataBidirectionalLocal(1024 * 1024) // 1 MB
            .initialMaxStreamDataBidirectionalRemote(1024 * 1024) // 1 MB
            .initialMaxStreamsBidirectional(NetworkInternalConnector.config.quicConfig.maxStreams)
            .congestionControlAlgorithm(QuicCongestionControlAlgorithm.BBR)
            .handler(QuicCertificateHandler(logger))
            .streamHandler(PlayerConnectionInitializer(logger))
            .build()

        ctx.channel().pipeline().addLast(quicCodec)

        logger.info("initialized for channel: ${ctx.channel().localAddress()}")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error("Error in Datagram Handler", cause)
        ctx.close()
    }
}

private class QuicCertificateHandler(private val logger: Logger) : ChannelInboundHandlerAdapter() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel() as QuicChannel
        logger.info("Received connection from ${ctx.channel().remoteAddress()}")
        val clientCertificate = extractClientCertificate(channel)
        if (clientCertificate == null) {
            logger.warn("Client certificate is null from ${ctx.channel().remoteAddress()}")
            return
        }
        channel.attr(QuicTransport.CLIENT_CERTIFICATE_ATTR).set(clientCertificate);
        logger.info("Client ${ctx.channel().remoteAddress()} certificated")
    }

    private fun extractClientCertificate(channel: QuicChannel): X509Certificate? {
        try {
            val sslEngine = channel.sslEngine() ?: return null
            val peerCerts: Array<Certificate?>? = sslEngine.session.peerCertificates
            if (!peerCerts.isNullOrEmpty() && peerCerts[0] is X509Certificate) {
                return peerCerts[0] as X509Certificate?
            }
        } catch (e: Exception) {
            logger.error("No peer certificate available", e.message)
        }
        return null
    }
}
