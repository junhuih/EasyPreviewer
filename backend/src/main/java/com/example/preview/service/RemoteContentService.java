package com.example.preview.service;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class RemoteContentService {

    public void validateUrl(String sourceUrl) {
        URI uri = URI.create(sourceUrl);
        String scheme = uri.getScheme();
        if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new IllegalArgumentException("preview.invalid.url");
        }
    }

    public InputStream openStream(String sourceUrl) throws Exception {
        return new URL(sourceUrl).openStream();
    }

    public Path downloadToTempFile(String sourceUrl, String fileName) throws Exception {
        String suffix = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.'))
                : ".bin";
        Path tempFile = Files.createTempFile("preview-src-", suffix);
        try (InputStream inputStream = openStream(sourceUrl)) {
            Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }
}

