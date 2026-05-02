package codes.reactor.network.internal.quic.certificate

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.math.BigInteger
import java.security.KeyPair
import java.security.cert.X509Certificate
import java.time.Duration
import java.util.Date

object SelfSignedCertGenerator {

    @Throws(Exception::class)
    fun generate(keyPair: KeyPair): X509Certificate? {
        val now = System.currentTimeMillis()

        val subject = X500Name("CN=hytale-server")
        val serial: BigInteger = BigInteger.valueOf(now)

        val signer = JcaContentSignerBuilder("SHA256withRSA")
            .build(keyPair.private)

        val certBuilder: X509v3CertificateBuilder =
            JcaX509v3CertificateBuilder(
                subject,
                serial,
                Date(now - 1000L),
                Date(now + Duration.ofDays(3650).toMillis()),
                subject,
                keyPair.public
            )

        return JcaX509CertificateConverter()
            .setProvider("BC")
            .getCertificate(certBuilder.build(signer))
    }
}
