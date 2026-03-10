package com.chronicle.service;

import com.chronicle.entity.Entry.EntryStatus;
import com.chronicle.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final EntryRepository entryRepository;

    public Map<String, Object> getProgress() {
        long total = entryRepository.count();
        long completed = entryRepository.findByStatus(EntryStatus.COMPLETED).size();
        long inReview = entryRepository.findByStatus(EntryStatus.IN_REVIEW).size();
        long notStarted = entryRepository.findByStatus(EntryStatus.NOT_STARTED).size();
        long inProgress = entryRepository.findByStatus(EntryStatus.IN_PROGRESS).size();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("completed", completed);
        result.put("inReview", inReview);
        result.put("notStarted", notStarted);
        result.put("inProgress", inProgress);
        result.put("completionRate", total > 0 ? (double) completed / total * 100 : 0);
        return result;
    }

    public Map<String, Object> getDepartmentStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("departments", java.util.List.of());
        return result;
    }

    public Map<String, Object> getWorkload() {
        Map<String, Object> result = new HashMap<>();
        result.put("editors", java.util.List.of());
        result.put("contributors", java.util.List.of());
        return result;
    }
}
