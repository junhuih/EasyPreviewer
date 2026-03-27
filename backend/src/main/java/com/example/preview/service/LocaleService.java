package com.example.preview.service;

import com.example.preview.config.PreviewProperties;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Service
public class LocaleService {

    private final PreviewProperties previewProperties;

    private final Map<String, Map<String, String>> messages = Map.of(
            "en", Map.ofEntries(
                    Map.entry("preview.ready", "Preview is ready."),
                    Map.entry("preview.failed", "Preview failed. Check LibreOffice or the source file."),
                    Map.entry("preview.unsupported.cad", "CAD preview is intentionally not supported in v1."),
                    Map.entry("preview.unsupported.tiff", "TIFF conversion is intentionally not supported in v1."),
                    Map.entry("preview.unsupported.media", "Server-side media transcoding is intentionally not supported in v1."),
                    Map.entry("preview.unsupported.general", "This file type is not supported in this preview-only product."),
                    Map.entry("preview.office.missing", "LibreOffice is required for Office preview but is not available."),
                    Map.entry("preview.invalid.url", "Only HTTP and HTTPS source URLs are allowed.")
            ),
            "zh", Map.ofEntries(
                    Map.entry("preview.ready", "预览已就绪。"),
                    Map.entry("preview.failed", "预览失败，请检查 LibreOffice 配置或源文件是否可用。"),
                    Map.entry("preview.unsupported.cad", "V1 阶段明确不支持 CAD 预览。"),
                    Map.entry("preview.unsupported.tiff", "V1 阶段明确不支持 TIFF 转换预览。"),
                    Map.entry("preview.unsupported.media", "V1 阶段明确不支持服务端音视频转码。"),
                    Map.entry("preview.unsupported.general", "当前预览产品不支持该文件类型。"),
                    Map.entry("preview.office.missing", "Office 预览依赖 LibreOffice，但当前环境不可用。"),
                    Map.entry("preview.invalid.url", "仅允许使用 HTTP 或 HTTPS 源地址。")
            )
    );

    public LocaleService(PreviewProperties previewProperties) {
        this.previewProperties = previewProperties;
    }

    public String normalizeLocale(String requestedLocale) {
        if (requestedLocale == null || requestedLocale.isBlank()) {
            return "en";
        }
        String language = Locale.forLanguageTag(requestedLocale).getLanguage();
        if (previewProperties.getSupportedLocales().contains(language)) {
            return language;
        }
        return "en";
    }

    public String getMessage(String locale, String key) {
        String normalized = normalizeLocale(locale);
        return messages.getOrDefault(normalized, messages.get("en"))
                .getOrDefault(key, messages.get("en").getOrDefault(key, key));
    }
}

