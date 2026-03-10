package com.chronicle.controller;

import com.chronicle.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> progress() {
        return ResponseEntity.ok(statisticsService.getProgress());
    }

    @GetMapping("/department")
    public ResponseEntity<Map<String, Object>> departmentStats() {
        return ResponseEntity.ok(statisticsService.getDepartmentStats());
    }

    @GetMapping("/workload")
    public ResponseEntity<Map<String, Object>> workload() {
        return ResponseEntity.ok(statisticsService.getWorkload());
    }
}
