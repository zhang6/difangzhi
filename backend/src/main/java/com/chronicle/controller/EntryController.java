package com.chronicle.controller;

import com.chronicle.entity.Entry;
import com.chronicle.entity.Entry.EntryStatus;
import com.chronicle.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    @GetMapping
    public List<Entry> list(
            @RequestParam(required = false) Long catalogId,
            @RequestParam(required = false) EntryStatus status) {
        return entryService.search(catalogId, status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entry> get(@PathVariable Long id) {
        return entryService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Entry create(@RequestBody Entry entry) {
        return entryService.save(entry);
    }

    @PutMapping("/{id}")
    public Entry update(@PathVariable Long id, @RequestBody Entry entry) {
        entry.setId(id);
        return entryService.save(entry);
    }

    @PutMapping("/{id}/status")
    public Entry updateStatus(@PathVariable Long id, @RequestBody Map<String, EntryStatus> body) {
        return entryService.updateStatus(id, body.get("status"));
    }

    @GetMapping("/{id}/versions")
    public List<com.chronicle.entity.EntryVersion> getVersions(@PathVariable Long id) {
        return entryService.getVersions(id);
    }

    @PostMapping("/{id}/ai-generate")
    public ResponseEntity<Map<String, String>> aiGenerate(@PathVariable Long id) {
        return ResponseEntity.ok(entryService.aiGenerate(id));
    }
}
