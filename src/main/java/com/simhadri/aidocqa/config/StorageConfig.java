// config/StorageConfig.java
package com.simhadri.aidocqa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class StorageConfig {

    private static final Logger log = LoggerFactory.getLogger(StorageConfig.class);

    @Value("${file.upload.temp-dir:./temp-files}")
    private String tempDir;

    @PostConstruct
    public void init() {
        try {
            Path tempPath = Paths.get(tempDir);
            if (!Files.exists(tempPath)) {
                Files.createDirectories(tempPath);
                log.info("Created temporary directory: {}", tempDir);
            }

            // Clean up old temp files on startup
            Files.list(tempPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            Files.deleteIfExists(file);
                            log.debug("Deleted old temp file: {}", file.getFileName());
                        } catch (IOException e) {
                            log.warn("Could not delete temp file: {}", file.getFileName());
                        }
                    });

            log.info("Storage configuration initialized successfully");
        } catch (IOException e) {
            log.error("Failed to create temporary directory: {}", e.getMessage());
        }
    }

    public Path getTempDir() {
        return Paths.get(tempDir);
    }
}