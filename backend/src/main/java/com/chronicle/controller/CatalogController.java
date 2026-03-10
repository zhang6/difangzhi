package com.chronicle.controller;

import com.chronicle.entity.Catalog;
import com.chronicle.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogs")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public List<Catalog> list(@RequestParam(required = false) Long parentId) {
        return catalogService.getTree(parentId);
    }

    @GetMapping("/tree")
    public List<Catalog> tree() {
        return catalogService.getFullTree();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Catalog> get(@PathVariable Long id) {
        return catalogService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Catalog create(@RequestBody Catalog catalog) {
        return catalogService.save(catalog);
    }

    @PutMapping("/{id}")
    public Catalog update(@PathVariable Long id, @RequestBody Catalog catalog) {
        catalog.setId(id);
        return catalogService.save(catalog);
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody List<com.chronicle.dto.CatalogReorderRequest> items) {
        catalogService.reorder(items);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        catalogService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
