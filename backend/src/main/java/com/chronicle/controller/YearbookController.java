package com.chronicle.controller;

import com.chronicle.dto.PageResult;
import com.chronicle.entity.YbYearbook;
import com.chronicle.entity.YbYearbookManager;
import com.chronicle.service.YearbookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/yearbooks")
@RequiredArgsConstructor
public class YearbookController {

    private final YearbookService yearbookService;

    @GetMapping
    public PageResult<YbYearbook> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        var pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());
        return PageResult.of(yearbookService.list(keyword, status, pageable));
    }

    @GetMapping("/{id}")
    public YbYearbook get(@PathVariable UUID id) {
        return yearbookService.getById(id);
    }

    @PostMapping
    public YbYearbook create(@RequestBody YbYearbook yb) {
        return yearbookService.create(yb);
    }

    @PutMapping("/{id}")
    public YbYearbook update(@PathVariable UUID id, @RequestBody YbYearbook yb) {
        return yearbookService.update(id, yb);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        yearbookService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/managers")
    public List<YbYearbookManager> getManagers(@PathVariable UUID id) {
        return yearbookService.getManagers(id);
    }

    @PostMapping("/{id}/managers")
    public YbYearbookManager setManager(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return yearbookService.setManager(id, UUID.fromString(body.get("userId")));
    }
}
