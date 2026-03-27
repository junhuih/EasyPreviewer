package com.example.preview.service;

import com.example.preview.config.PreviewProperties;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class OfficeConversionService {

    private final PreviewProperties previewProperties;

    public OfficeConversionService(PreviewProperties previewProperties) {
        this.previewProperties = previewProperties;
    }

    public Path convertToPdf(Path inputFile) throws Exception {
        File officeHome = null;
        String configuredHome = previewProperties.getOffice().getHome();
        if (configuredHome != null && !configuredHome.isBlank()) {
            officeHome = Path.of(configuredHome).toFile();
        }

        Path outputFile = Files.createTempFile("preview-office-", ".pdf");
        LocalOfficeManager officeManager = LocalOfficeManager.builder()
                .officeHome(officeHome)
                .install()
                .build();

        try {
            officeManager.start();
            LocalConverter.make(officeManager)
                    .convert(inputFile.toFile())
                    .to(outputFile.toFile())
                    .execute();
            return outputFile;
        } finally {
            officeManager.stop();
        }
    }
}

