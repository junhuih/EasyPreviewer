package com.example.preview.service;

import com.example.preview.api.CreatePreviewRequest;
import com.example.preview.model.PreviewCapability;
import com.example.preview.model.PreviewMode;
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
        String normalizedSourceUrl = remoteContentService.normalizeSourceUrl(request.sourceUrl());

        String locale = localeService.normalizeLocale(request.locale());
        String fileName = fileTypeService.resolveFileName(normalizedSourceUrl);
        String extension = fileTypeService.resolveExtension(fileName);
        PreviewCapability capability = capabilityRegistry.resolve(extension);

        PreviewSession session = new PreviewSession();
        session.setId(UUID.randomUUID().toString());
        session.setSourceUrl(normalizedSourceUrl);
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
                var inputPath = remoteContentService.downloadToTempFile(normalizedSourceUrl, fileName);
                if (useBrowserSpreadsheetViewer(extension)) {
                    session.setContentPath(inputPath);
                    session.setContentType(resolveContentType(extension));
                } else {
                    var outputPath = capability.previewMode() == PreviewMode.SPREADSHEET
                            ? officeConversionService.convertToHtml(inputPath)
                            : officeConversionService.convertToPdf(inputPath);
                    session.setContentPath(outputPath);
                    session.setContentType(capability.previewMode() == PreviewMode.SPREADSHEET
                            ? "text/html; charset=UTF-8"
                            : "application/pdf");
                }
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
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsm" -> "application/vnd.ms-excel.sheet.macroEnabled.12";
            case "ods" -> "application/vnd.oasis.opendocument.spreadsheet";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "webp" -> "image/webp";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "mov" -> "video/quicktime";
            case "m4v" -> "video/x-m4v";
            case "ogg" -> "video/ogg";
            case "csv" -> "text/csv; charset=UTF-8";
            case "md", "markdown", "txt", "json", "xml", "yaml", "yml", "java", "js", "ts", "tsx", "jsx", "py", "css", "html"
                    -> "text/plain; charset=UTF-8";
            default -> "application/octet-stream";
        };
    }

    public boolean useBrowserSpreadsheetViewer(String extension) {
        return "xlsx".equals(extension) || "xlsm".equals(extension);
    }
}
