package com.chronicle.controller;

import com.chronicle.dto.AiGenerateRequest;
import com.chronicle.dto.PageResult;
import com.chronicle.entity.*;
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
    public List<YbEntry> list(@RequestParam UUID outlineId) {
        return entryService.listByOutline(outlineId);
    }

    @GetMapping("/paged")
    public PageResult<YbEntry> listPaged(
            @RequestParam UUID outlineId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int pageSize) {
        var pageable = PageRequest.of(page - 1, pageSize, Sort.by("sortOrder"));
        return PageResult.of(entryService.listByOutlinePaged(outlineId, pageable));
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

    @GetMapping("/{id}/versions")
    public List<YbEntryVersion> getVersions(@PathVariable UUID id) {
        return entryService.getVersions(id);
    }

    @PostMapping("/{id}/versions")
    public YbEntryVersion createVersion(@PathVariable UUID id, @RequestBody YbEntryVersion version) {
        version.setEntryId(id);
        return entryService.createVersion(version);
    }

    @GetMapping("/{id}/annotations")
    public List<YbAnnotation> getAnnotations(@PathVariable UUID id) {
        return entryService.getAnnotations(id);
    }

    @PostMapping("/{id}/annotations")
    public YbAnnotation createAnnotation(@PathVariable UUID id, @RequestBody YbAnnotation annotation) {
        annotation.setEntryId(id);
        return entryService.createAnnotation(annotation);
    }

    @PutMapping("/annotations/{annotationId}/status")
    public YbAnnotation updateAnnotationStatus(
            @PathVariable UUID annotationId, @RequestBody Map<String, String> body) {
        return entryService.updateAnnotationStatus(annotationId, body.get("status"));
    }

    @PostMapping("/{id}/ai-generate")
    public Map<String, String> aiGenerate(@PathVariable UUID id, @RequestBody AiGenerateRequest req) {
        String content = aiService.generateEntry(req.getRawData(), req.getHistoryData());
        YbEntry entry = entryService.getById(id);
        entry.setAiContent(content);
        entry.setStatus("editing");
        entryService.update(id, entry);
        return Map.of("content", content);
    }

    @PostMapping("/ai-rewrite")
    public Map<String, String> aiRewrite(@RequestBody Map<String, String> body) {
        return Map.of("content", aiService.rewrite(body.get("text")));
    }

    @PostMapping("/ai-expand")
    public Map<String, String> aiExpand(@RequestBody Map<String, String> body) {
        return Map.of("content", aiService.expand(body.get("text")));
    }

    @PostMapping("/ai-detect")
    public List<Map<String, Object>> aiDetect(@RequestBody Map<String, String> body) {
        return aiService.detectConflicts(body.get("content"));
    }

    @PostMapping("/ai-bot")
    public Map<String, String> aiBot(@RequestBody Map<String, String> body) {
        return Map.of("answer", aiService.botAnswer(body.get("question")));
    }

    @GetMapping("/history")
    public List<YbHistoryData> searchHistory(@RequestParam String keyword) {
        return entryService.searchHistory(keyword);
    }
}
