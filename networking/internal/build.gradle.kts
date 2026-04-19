dependencies {
    compileOnly(project(":networking:api"))
    compileOnly(project(":networking:protocol"))
    compileOnly(project(":kernel:api"))

    api("org.bouncycastle:bcpkix-jdk18on:${findProperty("bouncyCastleVersion")}")
    api("com.github.luben:zstd-jni:${findProperty("zstdJniVersion")}")

    val incubatorVersion = findProperty("nettyIncubatorVersion")
    api("io.netty.incubator:netty-incubator-codec-native-quic:$incubatorVersion")
    api("io.netty.incubator:netty-incubator-codec-native-quic:$incubatorVersion:windows-x86_64")
    api("io.netty.incubator:netty-incubator-codec-native-quic:$incubatorVersion:linux-x86_64")

    val nettyVersion = findProperty("nettyVersion")
    api("io.netty:netty-handler:$nettyVersion")
    api("io.netty:netty-transport-native-epoll:$nettyVersion:linux-x86_64")
    api("io.netty:netty-transport-native-epoll:$nettyVersion:linux-aarch_64")
    api("io.netty:netty-transport-native-kqueue:$nettyVersion:osx-aarch_64")
}
