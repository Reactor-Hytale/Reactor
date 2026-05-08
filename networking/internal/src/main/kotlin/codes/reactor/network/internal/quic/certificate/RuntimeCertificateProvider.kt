package codes.reactor.network.internal.quic.certificate

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object RuntimeCertificateProvider {
    val PASSWORD = "hytale".toCharArray()

    fun loadOrCreate(path: Path): KeyStore {
        try {
            if (path.exists()) {
                return load(path)
            }

            path.parent?.createDirectories()
            val ks = generate()
            save(ks, path)
            return ks
        } catch (e: Exception) {
            throw RuntimeException("Failed to load or create server certificate", e)
        }
    }

    @Throws(Exception::class)
    private fun load(path: Path): KeyStore {
        val ks = KeyStore.getInstance("PKCS12")
        Files.newInputStream(path).use { `in` ->
            ks.load(`in`, PASSWORD)
        }
        return ks
    }

    @Throws(Exception::class)
    private fun save(ks: KeyStore, path: Path) {
        Files.newOutputStream(path).use { out ->
            ks.store(out, PASSWORD)
        }
    }

    @Throws(Exception::class)
    private fun generate(): KeyStore {
        Security.addProvider(BouncyCastleProvider())

        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(4096)
        val keyPair = keyGen.generateKeyPair()

        val cert: X509Certificate? = SelfSignedCertGenerator.generate(keyPair)

        val ks = KeyStore.getInstance("PKCS12")
        ks.load(null, null)
        ks.setKeyEntry("server", keyPair.private, PASSWORD, arrayOf<Certificate?>(cert))
        return ks
    }
}
