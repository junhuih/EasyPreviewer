package com.example.preview.service;

import com.example.preview.config.PreviewProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RemoteContentServiceTest {

    @Test
    void shouldResolveRelativePathAgainstConfiguredBaseUrl() {
        PreviewProperties properties = new PreviewProperties();
        properties.getRemote().setBaseUrl("https://files.example.com/root/");

        RemoteContentService service = new RemoteContentService(properties);

        String normalized = service.normalizeSourceUrl("reports/demo.docx");

        assertEquals("https://files.example.com/root/reports/demo.docx", normalized);
    }

    @Test
    void shouldRejectRelativePathWithoutBaseUrl() {
        PreviewProperties properties = new PreviewProperties();
        RemoteContentService service = new RemoteContentService(properties);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.normalizeSourceUrl("reports/demo.docx"));

        assertEquals("preview.remote.base-url.required", ex.getMessage());
    }

    @Test
    void shouldRejectHostOutsideAllowlist() {
        PreviewProperties properties = new PreviewProperties();
        properties.getRemote().setAllowedHosts(List.of("files.example.com"));

        RemoteContentService service = new RemoteContentService(properties);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.normalizeSourceUrl("https://cdn.example.com/demo.pdf"));

        assertEquals("preview.remote.host.not.allowed", ex.getMessage());
    }

    @Test
    void shouldAllowAbsoluteHttpUrlInAllowlist() {
        PreviewProperties properties = new PreviewProperties();
        properties.getRemote().setAllowedHosts(List.of("files.example.com"));

        RemoteContentService service = new RemoteContentService(properties);

        String normalized = service.normalizeSourceUrl("https://files.example.com/demo.pdf");

        assertEquals("https://files.example.com/demo.pdf", normalized);
    }
}
