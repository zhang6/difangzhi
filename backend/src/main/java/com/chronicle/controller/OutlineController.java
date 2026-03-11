package com.chronicle.controller;

import com.chronicle.entity.YbOutline;
import com.chronicle.service.OutlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/outlines")
@RequiredArgsConstructor
public class OutlineController {

    private final OutlineService outlineService;

    @GetMapping
    public List<YbOutline> list(@RequestParam UUID yearbookId) {
        return outlineService.listByYearbook(yearbookId);
    }

    @GetMapping("/my-tasks")
    public List<YbOutline> myTasks(@RequestParam UUID userId) {
        return outlineService.listByAssignedUser(userId);
    }

    @PostMapping
    public YbOutline create(@RequestBody YbOutline outline) {
        return outlineService.create(outline);
    }

    @PostMapping("/batch")
    public List<YbOutline> batchCreate(@RequestBody List<YbOutline> outlines) {
        return outlineService.batchCreate(outlines);
    }

    @PutMapping("/{id}")
    public YbOutline update(@PathVariable UUID id, @RequestBody YbOutline outline) {
        return outlineService.update(id, outline);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        outlineService.delete(id);
        return ResponseEntity.ok().build();
    }
}
