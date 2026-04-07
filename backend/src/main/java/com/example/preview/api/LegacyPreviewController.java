package com.example.preview.api;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class LegacyPreviewController {

    /**
     * Compatibility bridge for older callers that still send
     * /onlinePreview?url=<base64-encoded-source-url>.
     *
     * It does not fetch content itself. It only decodes the legacy
     * parameter and redirects into the modern ?fileUrl= flow while
     * preserving the active context path.
     */
    @GetMapping("/onlinePreview")
    public RedirectView onlinePreview(@RequestParam("url") String encodedUrl) {
        String sourceUrl = decodeSourceUrl(encodedUrl);
        String redirectUrl = currentContextPath()
                + "/?fileUrl="
                + UriUtils.encodeQueryParam(sourceUrl, StandardCharsets.UTF_8);
        RedirectView redirectView = new RedirectView(redirectUrl);
        redirectView.setExposeModelAttributes(false);
        redirectView.setStatusCode(HttpStatus.FOUND);
        return redirectView;
    }

    private String decodeSourceUrl(String encodedUrl) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedUrl);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid preview url", exception);
        }
    }

    private String currentContextPath() {
        String contextPath = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .getPath();
        return contextPath == null ? "" : contextPath;
    }
}
