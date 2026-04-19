dependencies {
    compileOnly(project(":networking:api"))
    compileOnly(project(":networking:protocol"))
    compileOnly(project(":kernel:api"))

    api("org.bouncycastle:bcpkix-jdk18on:${findProperty("bouncyCastleVersion") ?: "1.83"}")

    api("com.github.luben:zstd-jni:${findProperty("zstdJniVersion") ?: "1.5.7-6"}")

    api("io.netty:netty-handler:${findProperty("nettyVersion") ?: "4.2.9.Final"}")

    api("io.netty.incubator:netty-incubator-codec-native-quic:${findProperty("nettyIncubatorVersion") ?: "0.0.74.Final"}")
    api("io.netty.incubator:netty-incubator-codec-native-quic:${findProperty("nettyIncubatorVersion") ?: "0.0.74.Final"}:windows-x86_64")
    api("io.netty.incubator:netty-incubator-codec-native-quic:${findProperty("nettyIncubatorVersion") ?: "0.0.74.Final"}:linux-x86_64")

    api("io.netty:netty-transport-native-epoll:${findProperty("nettyVersion") ?: "4.2.9.Final"}:linux-x86_64")
    api("io.netty:netty-transport-native-epoll:${findProperty("nettyVersion") ?: "4.2.9.Final"}:linux-aarch_64")
    api("io.netty:netty-transport-native-kqueue:${findProperty("nettyVersion") ?: "4.2.9.Final"}:osx-aarch_64")
}
