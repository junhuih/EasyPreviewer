package com.example.preview.api;

import com.example.preview.model.PreviewMode;
import com.example.preview.model.PreviewStatus;

public record PreviewSessionResponse(
        String id,
        String fileName,
        String extension,
        String locale,
        PreviewStatus status,
        PreviewMode previewMode,
        boolean supported,
        boolean conversionRequired,
        String message,
        String contentUrl
) {
}

