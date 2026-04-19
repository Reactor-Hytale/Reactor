package ink.reactor.launcher.logger.file;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public final class LogCompressor {

    public static void compress(final Path filePath, final boolean compress) throws IOException {
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }

        final Path parentPath = filePath.getParent();
        final BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);

        final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.US)
            .withZone(ZoneId.systemDefault());

        final String formattedDate = formatter.format(attributes.lastModifiedTime().toInstant());

        final String baseFileName = (parentPath == null)
            ? formattedDate
            : parentPath.resolve(formattedDate).toString();

        if (!compress) {
            Files.move(filePath, Path.of(baseFileName + ".log"), StandardCopyOption.REPLACE_EXISTING);
            return;
        }

        final Path zipPath = Path.of(baseFileName + ".zip");
        final URI zipUri = URI.create("jar:" + zipPath.toUri());

        try (FileSystem zipfs = FileSystems.newFileSystem(zipUri, Map.of("create", "true"))) {
            final Path fileInZip = zipfs.getPath(filePath.getFileName().toString());
            final Path parentInZip = fileInZip.getParent();

            if (parentInZip != null) {
                Files.createDirectories(parentInZip);
            }

            Files.copy(filePath, fileInZip, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new IOException("Failed to create ZIP file", e);
        }

        Files.delete(filePath);
    }
}
