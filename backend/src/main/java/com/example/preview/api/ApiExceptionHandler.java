package com.example.preview.api;

import com.example.preview.service.LocaleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final LocaleService localeService;

    public ApiExceptionHandler(LocaleService localeService) {
        this.localeService = localeService;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        String key = ex.getMessage() == null ? "preview.failed" : ex.getMessage();
        return ResponseEntity.badRequest().body(Map.of(
                "status", "FAILED",
                "messageKey", key,
                "messageEn", localeService.getMessage("en", key),
                "messageZh", localeService.getMessage("zh", key)
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", "FAILED",
                "messageKey", "preview.failed",
                "messageEn", "The request is invalid.",
                "messageZh", "请求参数无效。"
        ));
    }
}

