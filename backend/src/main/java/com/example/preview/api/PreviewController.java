package com.example.preview.api;

import com.example.preview.model.PreviewSession;
import com.example.preview.model.PreviewStatus;
import com.example.preview.service.PreviewSessionService;
import com.example.preview.service.RemoteContentService;
import com.example.preview.service.SpreadsheetDisplayService;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/previews")
public class PreviewController {

    private static final Pattern HTML_ASSET_PATTERN = Pattern.compile(
            "(?<attr>src|href)=(?<quote>[\"'])(?![a-zA-Z]+:|/|#)(?<path>[^\"'#?]+)(?<suffix>[^\"']*)(?<quoteEnd>[\"'])"
    );

    private final PreviewSessionService previewSessionService;
    private final RemoteContentService remoteContentService;
    private final SpreadsheetDisplayService spreadsheetDisplayService;

    public PreviewController(
            PreviewSessionService previewSessionService,
            RemoteContentService remoteContentService,
            SpreadsheetDisplayService spreadsheetDisplayService
    ) {
        this.previewSessionService = previewSessionService;
        this.remoteContentService = remoteContentService;
        this.spreadsheetDisplayService = spreadsheetDisplayService;
    }

    @PostMapping("/resolve")
    public PreviewSessionResponse resolve(@Valid @RequestBody CreatePreviewRequest request) {
        PreviewSession session = previewSessionService.create(request);
        return toResponse(session);
    }

    @GetMapping("/{id}")
    public PreviewSessionResponse get(@PathVariable String id) {
        return toResponse(previewSessionService.getRequired(id));
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<?> content(@PathVariable String id,
                                     @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) throws Exception {
        PreviewSession session = previewSessionService.getRequired(id);
        if (session.getStatus() != PreviewStatus.READY) {
            return ResponseEntity.badRequest().build();
        }

        if (session.getContentPath() != null && session.getContentType() != null
                && session.getContentType().startsWith("text/html")) {
            String html = Files.readString(session.getContentPath(), StandardCharsets.UTF_8);
            String rewritten = rewriteHtmlAssetUrls(id, html);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + session.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType("text/html; charset=UTF-8"))
                    .body(rewritten);
        }

        MediaType mediaType = MediaType.parseMediaType(session.getContentType());
        ResponseEntity.BodyBuilder builder;
        if (session.getContentPath() != null) {
            return buildLocalFileResponse(session, mediaType, rangeHeader);
        } else {
            return buildRemoteResponse(session, mediaType, rangeHeader);
        }
    }

    @GetMapping("/{id}/assets/{assetName:.+}")
    public ResponseEntity<InputStreamResource> asset(@PathVariable String id, @PathVariable String assetName) throws Exception {
        PreviewSession session = previewSessionService.getRequired(id);
        if (session.getContentPath() == null || session.getStatus() != PreviewStatus.READY) {
            return ResponseEntity.notFound().build();
        }

        Path assetPath = session.getContentPath().getParent().resolve(assetName).normalize();
        if (!assetPath.startsWith(session.getContentPath().getParent()) || !Files.exists(assetPath) || Files.isDirectory(assetPath)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(Files.probeContentType(assetPath) != null
                        ? Files.probeContentType(assetPath)
                        : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .contentLength(Files.size(assetPath))
                .body(new InputStreamResource(Files.newInputStream(assetPath)));
    }

    @GetMapping("/{id}/spreadsheet-display")
    public SpreadsheetDisplayService.SpreadsheetDisplayResponse spreadsheetDisplay(@PathVariable String id) throws Exception {
        PreviewSession session = previewSessionService.getRequired(id);
        if (session.getContentPath() == null || session.getStatus() != PreviewStatus.READY) {
            throw new IllegalArgumentException("Preview session not found");
        }
        return spreadsheetDisplayService.extractDisplayValues(session.getContentPath(), session.getLocale());
    }

    private PreviewSessionResponse toResponse(PreviewSession session) {
        String contentUrl = contextPath() + "/api/previews/" + session.getId() + "/content";
        if (session.getCapability().previewMode() == com.example.preview.model.PreviewMode.SPREADSHEET
                && previewSessionService.useBrowserSpreadsheetViewer(session.getExtension())) {
            String fileUrl = UriUtils.encodePath(contentUrl, StandardCharsets.UTF_8);
            String locale = UriUtils.encodeQueryParam(session.getLocale(), StandardCharsets.UTF_8);
            String sessionId = UriUtils.encodeQueryParam(session.getId(), StandardCharsets.UTF_8);
            contentUrl = contextPath() + "/spreadsheet-viewer.html?file=" + fileUrl + "&lang=" + locale + "&session=" + sessionId;
        }
        return new PreviewSessionResponse(
                session.getId(),
                session.getFileName(),
                session.getExtension(),
                session.getLocale(),
                session.getStatus(),
                session.getCapability().previewMode(),
                session.getCapability().supported(),
                session.getCapability().conversionRequired(),
                previewSessionService.resolveMessage(session),
                contentUrl
        );
    }

    private String rewriteHtmlAssetUrls(String id, String html) {
        String assetPrefix = contextPath() + "/api/previews/" + id + "/assets/";
        return HTML_ASSET_PATTERN.matcher(html).replaceAll(matchResult ->
                matchResult.group("attr")
                        + "="
                        + matchResult.group("quote")
                        + assetPrefix + matchResult.group("path")
                        + matchResult.group("suffix")
                        + matchResult.group("quoteEnd")
        );
    }

    private String contextPath() {
        String contextPath = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .getPath();
        return contextPath == null ? "" : contextPath;
    }

    private ResponseEntity<InputStreamResource> buildLocalFileResponse(
            PreviewSession session,
            MediaType mediaType,
            String rangeHeader
    ) throws IOException {
        Path contentPath = session.getContentPath();
        long fileSize = Files.size(contentPath);
        RangeRequest rangeRequest = parseRange(rangeHeader, fileSize);
        InputStream inputStream = Files.newInputStream(contentPath);
        if (rangeRequest != null && rangeRequest.start > 0) {
            inputStream.skipNBytes(rangeRequest.start);
        }
        InputStream body = rangeRequest != null
                ? new LimitedInputStream(inputStream, rangeRequest.length())
                : inputStream;

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(rangeRequest != null ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + session.getFileName() + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentType(mediaType);
        if (rangeRequest != null) {
            builder.header(HttpHeaders.CONTENT_RANGE, rangeRequest.contentRange(fileSize))
                    .contentLength(rangeRequest.length());
        } else {
            builder.contentLength(fileSize);
        }
        return builder.body(new InputStreamResource(body));
    }

    private ResponseEntity<InputStreamResource> buildRemoteResponse(
            PreviewSession session,
            MediaType mediaType,
            String rangeHeader
    ) throws Exception {
        var response = remoteContentService.openResponse(session.getSourceUrl(), rangeHeader);
        long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
        ResponseEntity.BodyBuilder builder = ResponseEntity
                .status(response.statusCode() == HttpStatus.PARTIAL_CONTENT.value() ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + session.getFileName() + "\"")
                .contentType(mediaType)
                .header(HttpHeaders.ACCEPT_RANGES, response.headers().firstValue(HttpHeaders.ACCEPT_RANGES).orElse("bytes"));

        response.headers().firstValue(HttpHeaders.CONTENT_RANGE).ifPresent(value -> builder.header(HttpHeaders.CONTENT_RANGE, value));
        if (contentLength >= 0) {
            builder.contentLength(contentLength);
        }
        return builder.body(new InputStreamResource(response.body()));
    }

    private RangeRequest parseRange(String rangeHeader, long fileSize) {
        if (rangeHeader == null || rangeHeader.isBlank() || fileSize <= 0) {
            return null;
        }

        if (!rangeHeader.startsWith("bytes=")) {
            return null;
        }

        String spec = rangeHeader.substring("bytes=".length()).trim();
        int commaIndex = spec.indexOf(',');
        if (commaIndex >= 0) {
            spec = spec.substring(0, commaIndex).trim();
        }

        int dashIndex = spec.indexOf('-');
        if (dashIndex < 0) {
            return null;
        }

        String startPart = spec.substring(0, dashIndex).trim();
        String endPart = spec.substring(dashIndex + 1).trim();
        long start;
        long end;

        try {
            if (startPart.isEmpty()) {
                long suffixLength = Long.parseLong(endPart);
                if (suffixLength <= 0) {
                    return null;
                }
                start = Math.max(0, fileSize - suffixLength);
                end = fileSize - 1;
            } else {
                start = Long.parseLong(startPart);
                end = endPart.isEmpty() ? fileSize - 1 : Long.parseLong(endPart);
            }
        } catch (NumberFormatException ex) {
            return null;
        }

        if (start < 0 || end < start || start >= fileSize) {
            return null;
        }

        end = Math.min(end, fileSize - 1);
        return new RangeRequest(start, end);
    }

    private record RangeRequest(long start, long end) {
        long length() {
            return end - start + 1;
        }

        String contentRange(long fileSize) {
            return "bytes " + start + "-" + end + "/" + fileSize;
        }
    }

    private static final class LimitedInputStream extends FilterInputStream {
        private long remaining;

        private LimitedInputStream(InputStream delegate, long limit) {
            super(delegate);
            this.remaining = limit;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int value = super.read();
            if (value != -1) {
                remaining--;
            }
            return value;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int read = super.read(b, off, (int) Math.min(len, remaining));
            if (read > 0) {
                remaining -= read;
            }
            return read;
        }
    }
}
