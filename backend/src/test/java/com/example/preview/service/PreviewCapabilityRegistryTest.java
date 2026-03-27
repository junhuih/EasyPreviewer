package com.example.preview.service;

import com.example.preview.model.PreviewMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewCapabilityRegistryTest {

    private final PreviewCapabilityRegistry registry = new PreviewCapabilityRegistry();

    @Test
    void shouldSupportOfficeFilesWithConversion() {
        var capability = registry.resolve("docx");
        assertTrue(capability.supported());
        assertTrue(capability.conversionRequired());
        assertEquals(PreviewMode.PDF, capability.previewMode());
    }

    @Test
    void shouldSupportSpreadsheetFilesWithConversion() {
        var capability = registry.resolve("xlsx");
        assertTrue(capability.supported());
        assertTrue(capability.conversionRequired());
        assertEquals(PreviewMode.SPREADSHEET, capability.previewMode());
    }

    @Test
    void shouldSupportMp4AsVideo() {
        var capability = registry.resolve("mp4");
        assertTrue(capability.supported());
        assertFalse(capability.conversionRequired());
        assertEquals(PreviewMode.VIDEO, capability.previewMode());
    }

    @Test
    void shouldSupportCsvWithoutConversion() {
        var capability = registry.resolve("csv");
        assertTrue(capability.supported());
        assertFalse(capability.conversionRequired());
        assertEquals(PreviewMode.SPREADSHEET, capability.previewMode());
    }

    @Test
    void shouldMarkCadAsUnsupported() {
        var capability = registry.resolve("dwg");
        assertFalse(capability.supported());
        assertEquals(PreviewMode.UNSUPPORTED, capability.previewMode());
        assertEquals("preview.unsupported.cad", capability.messageKey());
    }
}
