package com.chronicle.controller;

import com.chronicle.dto.AiRequest;
import com.chronicle.dto.PageResult;
import com.chronicle.entity.YbAnnotation;
import com.chronicle.entity.YbEntry;
import com.chronicle.entity.YbEntryVersion;
import com.chronicle.entity.YbHistoryData;
import com.chronicle.service.AiMockService;
import com.chronicle.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;
    private final AiMockService aiService;

    @GetMapping
    public PageResult<YbEntry> list(
            @RequestParam UUID outlineId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        var pageable = PageRequest.of(page - 1, pageSize, Sort.by("sortOrder").ascending());
        return PageResult.of(entryService.listByOutline(outlineId, pageable));
    }

    @GetMapping("/{id}")
    public YbEntry get(@PathVariable UUID id) {
        return entryService.getById(id);
    }

    @PostMapping
    public YbEntry create(@RequestBody YbEntry entry) {
        return entryService.create(entry);
    }

    @PutMapping("/{id}")
    public YbEntry update(@PathVariable UUID id, @RequestBody YbEntry entry) {
        return entryService.update(id, entry);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        entryService.delete(id);
        return ResponseEntity.ok().build();
    }

    // Versions
    @GetMapping("/{id}/versions")
    public List<YbEntryVersion> listVersions(@PathVariable UUID id) {
        return entryService.listVersions(id);
    }

    @PostMapping("/{id}/versions")
    public YbEntryVersion createVersion(@PathVariable UUID id, @RequestBody YbEntryVersion version) {
        version.setEntryId(id);
        return entryService.createVersion(version);
    }

    @DeleteMapping("/versions/{versionId}")
    public ResponseEntity<Void> deleteVersion(@PathVariable UUID versionId) {
        entryService.deleteVersion(versionId);
        return ResponseEntity.ok().build();
    }

    // Annotations
    @GetMapping("/{id}/annotations")
    public List<YbAnnotation> listAnnotations(@PathVariable UUID id) {
        return entryService.listAnnotations(id);
    }

    @PostMapping("/{id}/annotations")
    public YbAnnotation createAnnotation(@PathVariable UUID id, @RequestBody YbAnnotation annotation) {
        annotation.setEntryId(id);
        return entryService.createAnnotation(annotation);
    }

    @PutMapping("/annotations/{annotationId}")
    public YbAnnotation updateAnnotation(@PathVariable UUID annotationId, @RequestBody YbAnnotation annotation) {
        return entryService.updateAnnotation(annotationId, annotation);
    }

    @DeleteMapping("/annotations/{annotationId}")
    public ResponseEntity<Void> deleteAnnotation(@PathVariable UUID annotationId) {
        entryService.deleteAnnotation(annotationId);
        return ResponseEntity.ok().build();
    }

    // History
    @GetMapping("/history")
    public List<YbHistoryData> searchHistory(@RequestParam String keyword) {
        return entryService.searchHistory(keyword);
    }

    // AI endpoints
    @PostMapping("/ai/generate")
    public Map<String, String> aiGenerate(@RequestBody AiRequest req) {
        String result = aiService.generateEntry(req.getRawData(), req.getHistoryData());
        return Map.of("content", result);
    }

    @PostMapping("/ai/rewrite")
    public Map<String, String> aiRewrite(@RequestBody AiRequest req) {
        String result = aiService.rewrite(req.getContent());
        return Map.of("content", result);
    }

    @PostMapping("/ai/expand")
    public Map<String, String> aiExpand(@RequestBody AiRequest req) {
        String result = aiService.expand(req.getContent());
        return Map.of("content", result);
    }

    @PostMapping("/ai/detect")
    public List<Map<String, Object>> aiDetect(@RequestBody AiRequest req) {
        return aiService.detectConflicts(req.getContent());
    }

    @PostMapping("/ai/bot")
    public Map<String, String> aiBot(@RequestBody AiRequest req) {
        String answer = aiService.botAnswer(req.getPrompt());
        return Map.of("answer", answer);
    }
}
