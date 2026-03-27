package com.example.preview.service;

import com.example.preview.model.PreviewCapability;
import com.example.preview.model.PreviewMode;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class PreviewCapabilityRegistry {

    private static final Set<String> OFFICE_EXTENSIONS = Set.of(
            "doc", "docx", "ppt", "pptx", "odt", "odp", "wps"
    );
    private static final Set<String> SPREADSHEET_EXTENSIONS = Set.of(
            "xls", "xlsx", "xlsm", "xlt", "xltm", "ods"
    );
    private static final Set<String> CSV_EXTENSIONS = Set.of("csv");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "webp", "svg"
    );
    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "webm", "mov", "m4v", "ogg"
    );
    private static final Set<String> TEXT_EXTENSIONS = Set.of(
            "txt", "json", "xml", "yaml", "yml", "java", "js", "ts", "tsx", "jsx", "py", "css", "html", "csv"
    );
    private static final Set<String> MARKDOWN_EXTENSIONS = Set.of("md", "markdown");
    private static final Set<String> CAD_EXTENSIONS = Set.of("dwg", "dxf", "dwf", "ifc");
    private static final Set<String> TIFF_EXTENSIONS = Set.of("tif", "tiff");
    private static final Set<String> MEDIA_EXTENSIONS = Set.of("avi", "rm", "wmv", "mkv", "flv", "3gp");

    private final Map<String, PreviewCapability> exactCapabilities = Map.of(
            "pdf", new PreviewCapability(true, PreviewMode.PDF, false, "preview.ready")
    );

    public PreviewCapability resolve(String extension) {
        if (exactCapabilities.containsKey(extension)) {
            return exactCapabilities.get(extension);
        }
        if (OFFICE_EXTENSIONS.contains(extension)) {
            return new PreviewCapability(true, PreviewMode.PDF, true, "preview.ready");
        }
        if (SPREADSHEET_EXTENSIONS.contains(extension)) {
            return new PreviewCapability(true, PreviewMode.SPREADSHEET, true, "preview.ready");
        }
        if (CSV_EXTENSIONS.contains(extension)) {
            return new PreviewCapability(true, PreviewMode.SPREADSHEET, false, "preview.ready");
        }
        if (IMAGE_EXTENSIONS.contains(extension)) {
            return new PreviewCapability(true, PreviewMode.IMAGE, false, "preview.ready");
        }
        if (VIDEO_EXTENSIONS.contains(extension)) {
            return new PreviewCapability(true, PreviewMode.VIDEO, false, "preview.ready");
        }
        if (MARKDOWN_EXTENSIONS.contains(extension)) {
            return new PreviewCapability(true, PreviewMode.MARKDOWN, false, "preview.ready");
        }
        if (TEXT_EXTENSIONS.contains(extension)) {
            return new PreviewCapability(true, PreviewMode.TEXT, false, "preview.ready");
        }
        if (CAD_EXTENSIONS.contains(extension)) {
            return new PreviewCapability(false, PreviewMode.UNSUPPORTED, false, "preview.unsupported.cad");
        }
        if (TIFF_EXTENSIONS.contains(extension)) {
            return new PreviewCapability(false, PreviewMode.UNSUPPORTED, false, "preview.unsupported.tiff");
        }
        if (MEDIA_EXTENSIONS.contains(extension)) {
            return new PreviewCapability(false, PreviewMode.UNSUPPORTED, false, "preview.unsupported.media");
        }
        return new PreviewCapability(false, PreviewMode.UNSUPPORTED, false, "preview.unsupported.general");
    }
}
