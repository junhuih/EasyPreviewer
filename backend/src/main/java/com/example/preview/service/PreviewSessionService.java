package com.example.preview.service;

import com.example.preview.api.CreatePreviewRequest;
import com.example.preview.model.PreviewCapability;
import com.example.preview.model.PreviewSession;
import com.example.preview.model.PreviewStatus;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class PreviewSessionService {

    private final CacheManager cacheManager;
    private final LocaleService localeService;
    private final FileTypeService fileTypeService;
    private final PreviewCapabilityRegistry capabilityRegistry;
    private final RemoteContentService remoteContentService;
    private final OfficeConversionService officeConversionService;

    public PreviewSessionService(
            CacheManager cacheManager,
            LocaleService localeService,
            FileTypeService fileTypeService,
            PreviewCapabilityRegistry capabilityRegistry,
            RemoteContentService remoteContentService,
            OfficeConversionService officeConversionService
    ) {
        this.cacheManager = cacheManager;
        this.localeService = localeService;
        this.fileTypeService = fileTypeService;
        this.capabilityRegistry = capabilityRegistry;
        this.remoteContentService = remoteContentService;
        this.officeConversionService = officeConversionService;
    }

    public PreviewSession create(CreatePreviewRequest request) {
        remoteContentService.validateUrl(request.sourceUrl());

        String locale = localeService.normalizeLocale(request.locale());
        String fileName = fileTypeService.resolveFileName(request.sourceUrl());
        String extension = fileTypeService.resolveExtension(fileName);
        PreviewCapability capability = capabilityRegistry.resolve(extension);

        PreviewSession session = new PreviewSession();
        session.setId(UUID.randomUUID().toString());
        session.setSourceUrl(request.sourceUrl());
        session.setFileName(fileName);
        session.setExtension(extension);
        session.setLocale(locale);
        session.setCapability(capability);
        session.setCreatedAt(Instant.now());
        session.setMessageKey(capability.messageKey());

        if (!capability.supported()) {
            session.setStatus(PreviewStatus.UNSUPPORTED);
            put(session);
            return session;
        }

        if (capability.conversionRequired()) {
            try {
                session.setStatus(PreviewStatus.PROCESSING);
                var inputPath = remoteContentService.downloadToTempFile(request.sourceUrl(), fileName);
                var outputPath = officeConversionService.convertToPdf(inputPath);
                session.setContentPath(outputPath);
                session.setContentType("application/pdf");
                session.setStatus(PreviewStatus.READY);
            } catch (Exception ex) {
                session.setStatus(PreviewStatus.FAILED);
                session.setMessageKey("preview.failed");
            }
        } else {
            session.setStatus(PreviewStatus.READY);
            session.setContentType(resolveContentType(extension));
        }

        put(session);
        return session;
    }

    public PreviewSession getRequired(String id) {
        Cache.ValueWrapper valueWrapper = Objects.requireNonNull(cacheManager.getCache("previewSessions"))
                .get(id);
        if (valueWrapper == null) {
            throw new IllegalArgumentException("Preview session not found");
        }
        return (PreviewSession) valueWrapper.get();
    }

    public String resolveMessage(PreviewSession session) {
        return localeService.getMessage(session.getLocale(), session.getMessageKey());
    }

    private void put(PreviewSession session) {
        Objects.requireNonNull(cacheManager.getCache("previewSessions")).put(session.getId(), session);
    }

    private String resolveContentType(String extension) {
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "webp" -> "image/webp";
            case "md", "markdown", "txt", "json", "xml", "yaml", "yml", "java", "js", "ts", "tsx", "jsx", "py", "css", "html", "csv"
                    -> "text/plain; charset=UTF-8";
            default -> "application/octet-stream";
        };
    }
}

