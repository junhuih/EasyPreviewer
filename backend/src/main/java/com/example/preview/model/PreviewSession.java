package com.example.preview.model;

import java.nio.file.Path;
import java.time.Instant;

public class PreviewSession {

    private String id;
    private String sourceUrl;
    private String fileName;
    private String extension;
    private String locale;
    private PreviewCapability capability;
    private PreviewStatus status;
    private String contentType;
    private Path contentPath;
    private String messageKey;
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public PreviewCapability getCapability() {
        return capability;
    }

    public void setCapability(PreviewCapability capability) {
        this.capability = capability;
    }

    public PreviewStatus getStatus() {
        return status;
    }

    public void setStatus(PreviewStatus status) {
        this.status = status;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Path getContentPath() {
        return contentPath;
    }

    public void setContentPath(Path contentPath) {
        this.contentPath = contentPath;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

