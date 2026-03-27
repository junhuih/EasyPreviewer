package com.example.preview.api;

import com.example.preview.model.PreviewSession;
import com.example.preview.model.PreviewStatus;
import com.example.preview.service.PreviewSessionService;
import com.example.preview.service.RemoteContentService;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
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

    public PreviewController(PreviewSessionService previewSessionService, RemoteContentService remoteContentService) {
        this.previewSessionService = previewSessionService;
        this.remoteContentService = remoteContentService;
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
    public ResponseEntity<?> content(@PathVariable String id) throws Exception {
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

        InputStream inputStream;
        long contentLength = -1;
        if (session.getContentPath() != null) {
            inputStream = Files.newInputStream(session.getContentPath());
            contentLength = Files.size(session.getContentPath());
        } else {
            inputStream = remoteContentService.openStream(session.getSourceUrl());
        }

        MediaType mediaType = MediaType.parseMediaType(session.getContentType());
        var builder = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + session.getFileName() + "\"")
                .contentType(mediaType);
        if (contentLength >= 0) {
            builder.contentLength(contentLength);
        }
        return builder.body(new InputStreamResource(inputStream));
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

    private PreviewSessionResponse toResponse(PreviewSession session) {
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
                "/api/previews/" + session.getId() + "/content"
        );
    }

    private String rewriteHtmlAssetUrls(String id, String html) {
        return HTML_ASSET_PATTERN.matcher(html).replaceAll(matchResult ->
                matchResult.group("attr")
                        + "="
                        + matchResult.group("quote")
                        + "/api/previews/" + id + "/assets/" + matchResult.group("path")
                        + matchResult.group("suffix")
                        + matchResult.group("quoteEnd")
        );
    }
}
