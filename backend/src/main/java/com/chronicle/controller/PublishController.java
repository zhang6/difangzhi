package com.chronicle.controller;

import com.chronicle.service.PublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publish")
@RequiredArgsConstructor
public class PublishController {

    private final PublishService publishService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(defaultValue = "word") String format,
            @RequestParam(required = false) List<Long> catalogIds,
            @RequestParam(required = false) Long entryId) {
        byte[] content = publishService.export(format, catalogIds, entryId);
        String contentType = switch (format.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "html" -> "text/html";
            case "markdown" -> "text/markdown";
            default -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        };
        String ext = switch (format.toLowerCase()) {
            case "pdf" -> ".pdf";
            case "html" -> ".html";
            case "markdown" -> ".md";
            default -> ".docx";
        };
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=chronicle" + ext)
            .contentType(MediaType.parseMediaType(contentType))
            .body(content);
    }
}
