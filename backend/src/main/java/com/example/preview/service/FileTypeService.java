package com.example.preview.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class FileTypeService {

    public String resolveFileName(String sourceUrl) {
        try {
            String path = URI.create(sourceUrl).getPath();
            if (path == null || path.isBlank()) {
                return "remote-file";
            }
            String name = Path.of(path).getFileName().toString();
            return name.isBlank() ? "remote-file" : name;
        } catch (Exception ignored) {
            return "remote-file";
        }
    }

    public String resolveExtension(String fileName) {
        return Optional.ofNullable(fileName)
                .filter(name -> name.contains("."))
                .map(name -> name.substring(name.lastIndexOf('.') + 1).toLowerCase())
                .orElse("");
    }
}

