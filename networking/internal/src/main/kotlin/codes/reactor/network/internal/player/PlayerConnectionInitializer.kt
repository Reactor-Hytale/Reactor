package codes.reactor.network.internal.player

import codes.reactor.kernel.Reactor
import codes.reactor.kernel.logger.Logger
import codes.reactor.network.internal.NetworkInternalConnector
import codes.reactor.network.internal.io.PacketDecoder
import codes.reactor.network.internal.io.PacketEncoder
import codes.reactor.network.internal.packet.PacketInboundHandler
import codes.reactor.network.internal.quic.QuicTransport
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.incubator.codec.quic.QuicChannel
import io.netty.incubator.codec.quic.QuicStreamChannel

class PlayerConnectionInitializer(private val logger: Logger): ChannelInitializer<Channel>() {

    override fun initChannel(ch: Channel) {
        if (ch is QuicStreamChannel) {
            val parentChannel: QuicChannel = ch.parent()
            val clientCert = parentChannel.attr(QuicTransport.CLIENT_CERTIFICATE_ATTR).get()

            if (clientCert != null) {
                ch.attr(QuicTransport.CLIENT_CERTIFICATE_ATTR).set(clientCert)
                logger.info("Copied client certificate to stream: ${clientCert.getSubjectX500Principal().name}")
            }
        }

        val logger = Reactor.loggerFactory.createLogger(ch.remoteAddress().toString())
        val connection = NettyPlayerConnection(ch, logger)

        ch.pipeline()
            .addLast("readTimeOut", ReadTimeoutHandler(NetworkInternalConnector.config.readTimeoutSeconds))
            .addLast("decoder", PacketDecoder(logger))
            .addLast("encoder", PacketEncoder())
            .addLast("packet_handler", PacketInboundHandler(connection))

        logger.info("Connection initialized.")
    }
}
