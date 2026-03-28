package com.example.preview.service;

import com.example.preview.config.PreviewProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class RemoteContentService {

    private static final Logger log = LoggerFactory.getLogger(RemoteContentService.class);

    private final PreviewProperties previewProperties;
    private final HttpClient httpClient;

    public RemoteContentService(PreviewProperties previewProperties) {
        this.previewProperties = previewProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(previewProperties.getRemote().getConnectTimeoutMs()))
                .followRedirects(previewProperties.getRemote().isAllowRedirects()
                        ? HttpClient.Redirect.NORMAL
                        : HttpClient.Redirect.NEVER)
                .build();
    }

    @PostConstruct
    void logRemoteConfiguration() {
        PreviewProperties.Remote remote = previewProperties.getRemote();
        log.info(
                "Remote file config loaded: baseUrl='{}', allowedHosts={}, connectTimeoutMs={}, readTimeoutMs={}, maxFileSizeBytes={}, allowRedirects={}",
                blankToEmpty(remote.getBaseUrl()),
                remote.getAllowedHosts(),
                remote.getConnectTimeoutMs(),
                remote.getReadTimeoutMs(),
                remote.getMaxFileSizeBytes(),
                remote.isAllowRedirects()
        );
    }

    public String normalizeSourceUrl(String sourceUrl) {
        try {
            URI uri = resolveUri(sourceUrl);
            String scheme = uri.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException("preview.invalid.url");
            }
            validateAllowedHost(uri);
            return uri.toString();
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("preview.")) {
                throw ex;
            }
            throw new IllegalArgumentException("preview.invalid.url");
        }
    }

    public InputStream openStream(String sourceUrl) throws Exception {
        HttpResponse<InputStream> response = sendRequest(normalizeSourceUrl(sourceUrl), HttpResponse.BodyHandlers.ofInputStream());
        long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
        validateContentLength(contentLength);
        return new BoundedInputStream(response.body(), previewProperties.getRemote().getMaxFileSizeBytes());
    }

    public Path downloadToTempFile(String sourceUrl, String fileName) throws Exception {
        String normalizedSourceUrl = normalizeSourceUrl(sourceUrl);
        String suffix = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.'))
                : ".bin";
        Path tempFile = Files.createTempFile("preview-src-", suffix);
        try (InputStream inputStream = openStream(normalizedSourceUrl)) {
            copyWithLimit(inputStream, tempFile);
        }
        return tempFile;
    }

    private URI resolveUri(String sourceUrl) {
        URI uri = URI.create(sourceUrl.trim());
        if (uri.isAbsolute()) {
            return uri;
        }

        String baseUrl = blankToEmpty(previewProperties.getRemote().getBaseUrl());
        if (baseUrl.isBlank()) {
            throw new IllegalArgumentException("preview.remote.base-url.required");
        }
        return URI.create(baseUrl).resolve(uri);
    }

    private void validateAllowedHost(URI uri) {
        List<String> allowedHosts = previewProperties.getRemote().getAllowedHosts();
        if (allowedHosts == null || allowedHosts.isEmpty()) {
            return;
        }

        List<String> normalizedAllowedHosts = allowedHosts.stream()
                .map(this::normalizeHost)
                .filter(host -> !host.isBlank())
                .toList();
        if (normalizedAllowedHosts.isEmpty()) {
            return;
        }

        String host = uri.getHost();
        if (host == null || normalizedAllowedHosts.stream().noneMatch(normalizeHost(host)::equals)) {
            throw new IllegalArgumentException("preview.remote.host.not.allowed");
        }
    }

    private <T> HttpResponse<T> sendRequest(String sourceUrl, HttpResponse.BodyHandler<T> bodyHandler) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(sourceUrl))
                .GET()
                .timeout(Duration.ofMillis(previewProperties.getRemote().getReadTimeoutMs()));

        Map<String, String> defaultHeaders = previewProperties.getRemote().getDefaultHeaders();
        if (defaultHeaders == null) {
            defaultHeaders = Map.of();
        }

        for (Map.Entry<String, String> entry : defaultHeaders.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isBlank()
                    && entry.getValue() != null && !entry.getValue().isBlank()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }

        HttpResponse<T> response = httpClient.send(builder.build(), bodyHandler);
        int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new IllegalArgumentException("preview.remote.fetch.failed");
        }
        return response;
    }

    private void validateContentLength(long contentLength) {
        long maxFileSizeBytes = previewProperties.getRemote().getMaxFileSizeBytes();
        if (maxFileSizeBytes > 0 && contentLength > maxFileSizeBytes) {
            throw new IllegalArgumentException("preview.remote.file.too.large");
        }
    }

    private void copyWithLimit(InputStream inputStream, Path targetFile) throws IOException {
        long maxFileSizeBytes = previewProperties.getRemote().getMaxFileSizeBytes();
        try (var outputStream = Files.newOutputStream(targetFile)) {
            byte[] buffer = new byte[8192];
            long totalBytes = 0;
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                totalBytes += read;
                if (maxFileSizeBytes > 0 && totalBytes > maxFileSizeBytes) {
                    throw new IllegalArgumentException("preview.remote.file.too.large");
                }
                outputStream.write(buffer, 0, read);
            }
        }
    }

    private String normalizeHost(String host) {
        return host == null ? "" : host.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class BoundedInputStream extends FilterInputStream {
        private final long maxBytes;
        private long totalBytesRead;

        private BoundedInputStream(InputStream delegate, long maxBytes) {
            super(delegate);
            this.maxBytes = maxBytes;
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            if (value != -1) {
                increment(1);
            }
            return value;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = super.read(b, off, len);
            if (read > 0) {
                increment(read);
            }
            return read;
        }

        private void increment(int read) throws IOException {
            totalBytesRead += read;
            if (maxBytes > 0 && totalBytesRead > maxBytes) {
                throw new IOException("preview.remote.file.too.large");
            }
        }
    }
}
