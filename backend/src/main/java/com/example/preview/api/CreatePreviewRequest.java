package com.example.preview.api;

import jakarta.validation.constraints.NotBlank;

public record CreatePreviewRequest(
        @NotBlank(message = "sourceUrl is required")
        String sourceUrl,
        String locale
) {
}

