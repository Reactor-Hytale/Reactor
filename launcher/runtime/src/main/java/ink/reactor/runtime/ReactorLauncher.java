package ink.reactor.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public final class ReactorLauncher {

    public static final Path CACHE_PATH = Paths.get("cache/public/");

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
                "ReactorLauncher must be executed from reactor.jar. Compile in gradle using the command: `./gradlew :launcher:runtime:build` and execute the generated jar in `launcher/runtime/build/runtime/`"
            );
        }

        return codeSource;
    }

    private static Path extractEmbeddedPublicJar(final Path reactorJar) throws Exception {
        try (JarFile jarFile = new JarFile(reactorJar.toFile())) {
            final Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                throw new IllegalStateException("No MANIFEST found in jar file. Corrupted jar");
            }

            final String compileID = manifest.getMainAttributes().getValue("compile-id");
            if (compileID == null) {
                throw new IllegalStateException("Missing compile-id in Manifest. Corrupted jar");
            }

            final Path publicJar = CACHE_PATH.resolve(compileID + ".jar");

            if (Files.isRegularFile(publicJar)) {
                return publicJar;
            }

            deleteFilesInCache();

            final JarEntry entry = jarFile.getJarEntry(EMBEDDED_PUBLIC_JAR);
            if (entry == null) {
                throw new IllegalStateException("Missing embedded jar. Corrupted jar");
            }

            Files.createDirectories(CACHE_PATH);
            try (InputStream input = jarFile.getInputStream(entry)) {
                Files.copy(input, publicJar);
            }

            return publicJar;
        }
    }

    private static void deleteFilesInCache() throws IOException {
        if (!Files.exists(CACHE_PATH)) {
            return;
        }

        try (final Stream<Path> paths = Files.walk(CACHE_PATH, 1)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                 try {
                     Files.delete(path);
                 } catch (Exception _) {}
            });
        }
    }
}
