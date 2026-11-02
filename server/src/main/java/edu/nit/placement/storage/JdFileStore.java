package edu.nit.placement.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.nit.placement.exception.ApiException;

/**
 * Stores uploaded job-description files on disk (per database/01-schema.sql,
 * ANNOUNCEMENTS uses JD_FILE_PATH rather than a BLOB column). The path stored
 * in the database is relative to the storage directory.
 */
@Component
public class JdFileStore {

    private final Path baseDir;

    public JdFileStore(@Value("${app.jd.storage-dir:uploads}") String storageDir) {
        this.baseDir = Paths.get(storageDir).toAbsolutePath().normalize();
    }

    /** Writes the bytes and returns the path to record in JD_FILE_PATH. */
    public String save(Long announcementId, String originalName, byte[] content) {
        try {
            Files.createDirectories(baseDir);
            String fileName = announcementId + "-" + sanitize(originalName) + "-"
                    + UUID.randomUUID().toString().substring(0, 8);
            Path target = baseDir.resolve(fileName).normalize();
            if (!target.startsWith(baseDir)) {
                throw ApiException.badRequest("Invalid file name");
            }
            Files.write(target, content);
            return fileName;
        } catch (IOException ex) {
            throw ApiException.badRequest("Could not store the job description file");
        }
    }

    public byte[] read(String storedPath) {
        try {
            Path file = baseDir.resolve(storedPath).normalize();
            if (!file.startsWith(baseDir) || !Files.exists(file)) {
                return null;
            }
            return Files.readAllBytes(file);
        } catch (IOException ex) {
            return null;
        }
    }

    /** Strips path separators and other characters that could escape the directory. */
    private String sanitize(String originalName) {
        String cleaned = originalName == null ? "jd" : originalName;
        cleaned = cleaned.replaceAll("[^A-Za-z0-9._-]", "_");
        return cleaned.length() > 80 ? cleaned.substring(cleaned.length() - 80) : cleaned;
    }
}
