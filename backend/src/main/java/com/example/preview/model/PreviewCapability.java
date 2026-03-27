package com.example.preview.model;

public record PreviewCapability(
        boolean supported,
        PreviewMode previewMode,
        boolean conversionRequired,
        String messageKey
) {
}

