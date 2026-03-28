package com.example.preview.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "preview")
public class PreviewProperties {

    @NotEmpty
    private List<String> supportedLocales = List.of("zh", "en");
    private Cache cache = new Cache();
    private Office office = new Office();
    private Remote remote = new Remote();

    public List<String> getSupportedLocales() {
        return supportedLocales;
    }

    public void setSupportedLocales(List<String> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public Remote getRemote() {
        return remote;
    }

    public void setRemote(Remote remote) {
        this.remote = remote;
    }

    public static class Cache {
        private long ttlMinutes = 30;

        public long getTtlMinutes() {
            return ttlMinutes;
        }

        public void setTtlMinutes(long ttlMinutes) {
            this.ttlMinutes = ttlMinutes;
        }
    }

    public static class Office {
        private String home = "";

        public String getHome() {
            return home;
        }

        public void setHome(String home) {
            this.home = home;
        }
    }

    public static class Remote {
        private String baseUrl = "";
        private List<String> allowedHosts = List.of();
        private Map<String, String> defaultHeaders = new LinkedHashMap<>();
        private long connectTimeoutMs = 5000;
        private long readTimeoutMs = 60000;
        private long maxFileSizeBytes = 104857600;
        private boolean allowRedirects = true;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public List<String> getAllowedHosts() {
            return allowedHosts;
        }

        public void setAllowedHosts(List<String> allowedHosts) {
            this.allowedHosts = allowedHosts;
        }

        public Map<String, String> getDefaultHeaders() {
            return defaultHeaders;
        }

        public void setDefaultHeaders(Map<String, String> defaultHeaders) {
            this.defaultHeaders = defaultHeaders;
        }

        public long getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(long connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public long getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(long readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        public long getMaxFileSizeBytes() {
            return maxFileSizeBytes;
        }

        public void setMaxFileSizeBytes(long maxFileSizeBytes) {
            this.maxFileSizeBytes = maxFileSizeBytes;
        }

        public boolean isAllowRedirects() {
            return allowRedirects;
        }

        public void setAllowRedirects(boolean allowRedirects) {
            this.allowRedirects = allowRedirects;
        }
    }
}
