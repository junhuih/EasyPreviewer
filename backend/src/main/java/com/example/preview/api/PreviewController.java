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
import java.nio.file.Files;

@RestController
@RequestMapping("/api/previews")
public class PreviewController {

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
    public ResponseEntity<InputStreamResource> content(@PathVariable String id) throws Exception {
        PreviewSession session = previewSessionService.getRequired(id);
        if (session.getStatus() != PreviewStatus.READY) {
            return ResponseEntity.badRequest().build();
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
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + session.getFileName() + "\"")
                .contentLength(contentLength)
                .contentType(mediaType)
                .body(new InputStreamResource(inputStream));
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
}

