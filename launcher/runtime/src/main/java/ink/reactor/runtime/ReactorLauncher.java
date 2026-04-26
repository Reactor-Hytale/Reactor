package ink.reactor.runtime;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ReactorLauncher {

    private static final String EMBEDDED_PUBLIC_JAR = "embedded/public.jar";
    private static final String MAIN_FUNCTION = "start";
    private static final String ENTRYPOINT_CLASS =
        "ink.reactor.launcher.MinimalReactorLauncherKt";

    static void main() throws Throwable {
        final Path reactorJar = resolveOwnJar();
        final Path publicJar = extractEmbeddedPublicJar(reactorJar);

        final URLClassLoader publicLoader = new URLClassLoader(
            "public",
            new URL[]{publicJar.toUri().toURL()},
            ClassLoader.getPlatformClassLoader()
        );

        final URLClassLoader internalLoader = new URLClassLoader(
            "internal",
            new URL[]{reactorJar.toUri().toURL()},
            publicLoader
        );

        final Thread thread = Thread.currentThread();
        final ClassLoader previous = thread.getContextClassLoader();

        thread.setContextClassLoader(internalLoader);

        try {
            launchEntrypoint(internalLoader, publicLoader);
        } finally {
            thread.setContextClassLoader(previous);
        }
    }

    private static void launchEntrypoint(
        final ClassLoader internalLoader,
        final ClassLoader publicLoader
    ) throws Throwable {
        final Class<?> entrypointClass = Class.forName(
            ENTRYPOINT_CLASS,
            true,
            internalLoader
        );

        final Method mainMethod = entrypointClass.getMethod(MAIN_FUNCTION, ClassLoader.class);

        if (!Modifier.isStatic(mainMethod.getModifiers())) {
            throw new IllegalStateException(
                "Entrypoint " + MAIN_FUNCTION + "(ClassLoader[] publicClassLoader) must be static: " + ENTRYPOINT_CLASS
            );
        }

        try {
            mainMethod.invoke(null, publicLoader);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    private static Path resolveOwnJar() throws Exception {
        final Path codeSource = Paths.get(
            ReactorLauncher.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
        ).toAbsolutePath().normalize();

        if (!Files.isRegularFile(codeSource)) {
            throw new IllegalStateException(
                "ReactorLauncher must be executed from reactor.jar. Compile in gradle using the command: `./gradlew :launcher:runtime:build` and execute the generated jar in `launcher/runtime/build/libs/`"
            );
        }

        return codeSource;
    }

    private static Path extractEmbeddedPublicJar(final Path reactorJar) throws Exception {
        final String hash = sha256(reactorJar).substring(0, 16);
        final Path cacheDir = defaultCacheDir().resolve(hash);
        final Path extractedPublicJar = cacheDir.resolve("public.jar");

        if (Files.isRegularFile(extractedPublicJar)) {
            return extractedPublicJar;
        }

        Files.createDirectories(cacheDir);

        try (JarFile jarFile = new JarFile(reactorJar.toFile())) {
            final JarEntry entry = jarFile.getJarEntry(EMBEDDED_PUBLIC_JAR);

            if (entry == null) {
                throw new IllegalStateException(
                    "Missing embedded public jar: " + EMBEDDED_PUBLIC_JAR
                );
            }

            try (InputStream input = jarFile.getInputStream(entry)) {
                Files.copy(input, extractedPublicJar);
            }
        }

        if (!Files.isRegularFile(extractedPublicJar)) {
            throw new IllegalStateException(
                "Failed to extract embedded public jar to: " + extractedPublicJar
            );
        }

        return extractedPublicJar;
    }

    private static Path defaultCacheDir() {
        final String localAppData = System.getenv("LOCALAPPDATA");

        if (localAppData != null && !localAppData.isBlank()) {
            return Paths.get(localAppData, "Reactor", "cache");
        }

        return Paths.get(System.getProperty("user.home"), ".reactor", "cache");
    }

    private static String sha256(final Path file) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(Files.readAllBytes(file)));
    }
}
