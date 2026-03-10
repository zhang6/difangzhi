package com.chronicle.controller;

import com.chronicle.entity.Material;
import com.chronicle.entity.Material.MaterialType;
import com.chronicle.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    public Page<Material> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) MaterialType type,
            @RequestParam(required = false) Integer year,
            Pageable pageable) {
        return materialService.search(title, type, year, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Material> get(@PathVariable Long id) {
        return materialService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Material create(@RequestBody Material material) {
        return materialService.save(material);
    }

    @PostMapping("/upload")
    public Material upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) MaterialType type) {
        return materialService.upload(file, title, source, year, type);
    }

    @PutMapping("/{id}")
    public Material update(@PathVariable Long id, @RequestBody Material material) {
        material.setId(id);
        return materialService.save(material);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        materialService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/ai-summary")
    public ResponseEntity<Map<String, String>> generateSummary(
            @PathVariable Long id,
            @RequestParam(defaultValue = "500") int length) {
        return ResponseEntity.ok(materialService.generateSummary(id, length));
    }
}
