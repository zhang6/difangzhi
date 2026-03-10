package com.chronicle.controller;

import com.chronicle.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(
            @RequestParam String query,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(knowledgeService.search(query, year, department, limit));
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        return ResponseEntity.ok(knowledgeService.ask(question));
    }
}
