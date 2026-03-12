package com.chronicle.service;

import com.chronicle.entity.YbYearbook;
import com.chronicle.entity.YbYearbookManager;
import com.chronicle.repository.OutlineRepository;
import com.chronicle.repository.YearbookManagerRepository;
import com.chronicle.repository.YearbookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class YearbookService {

    private final YearbookRepository yearbookRepo;
    private final YearbookManagerRepository managerRepo;
    private final OutlineRepository outlineRepo;

    public Page<YbYearbook> list(String keyword, String status, Pageable pageable) {
        String kw = (keyword != null && keyword.isBlank()) ? null : keyword;
        String st = (status != null && (status.isBlank() || status.equals("all"))) ? null : status;
        return yearbookRepo.findByFilter(kw, st, pageable);
    }

    public Map<String, Long> stats() {
        long total = yearbookRepo.count();
        long inProgress = yearbookRepo.countByStatus("in_progress");
        long completed = yearbookRepo.countByStatus("completed");
        long notStarted = yearbookRepo.countByStatus("not_started");
        return Map.of("total", total, "in_progress", inProgress, "completed", completed, "not_started", notStarted);
    }

    public YbYearbook getById(UUID id) {
        return yearbookRepo.findById(id).orElseThrow(() -> new RuntimeException("年鉴不存在"));
    }

    public YbYearbook create(YbYearbook yb) {
        return yearbookRepo.save(yb);
    }

    public YbYearbook update(UUID id, YbYearbook updates) {
        YbYearbook yb = getById(id);
        if (updates.getName() != null) yb.setName(updates.getName());
        if (updates.getStartDate() != null) yb.setStartDate(updates.getStartDate());
        if (updates.getEndDate() != null) yb.setEndDate(updates.getEndDate());
        if (updates.getCoverType() != null) yb.setCoverType(updates.getCoverType());
        if (updates.getCoverUrl() != null) yb.setCoverUrl(updates.getCoverUrl());
        if (updates.getStatus() != null) yb.setStatus(updates.getStatus());
        if (updates.getProgress() != null) yb.setProgress(updates.getProgress());
        return yearbookRepo.save(yb);
    }

    @Transactional
    public void delete(UUID id) {
        managerRepo.deleteByYearbookId(id);
        outlineRepo.deleteByYearbookId(id);
        yearbookRepo.deleteById(id);
    }

    public List<YbYearbookManager> getManagers(UUID yearbookId) {
        return managerRepo.findByYearbookId(yearbookId);
    }

    @Transactional
    public YbYearbookManager addManager(UUID yearbookId, UUID userId) {
        if (managerRepo.existsByYearbookIdAndUserId(yearbookId, userId)) {
            return managerRepo.findByYearbookId(yearbookId).stream()
                    .filter(m -> m.getUserId().equals(userId)).findFirst().orElseThrow();
        }
        YbYearbookManager m = new YbYearbookManager();
        m.setYearbookId(yearbookId);
        m.setUserId(userId);
        return managerRepo.save(m);
    }

    @Transactional
    public void removeManager(UUID yearbookId, UUID userId) {
        managerRepo.findByYearbookId(yearbookId).stream()
                .filter(m -> m.getUserId().equals(userId))
                .forEach(m -> managerRepo.deleteById(m.getId()));
    }

    public void recalculateProgress(UUID yearbookId) {
        long total = outlineRepo.countByYearbookId(yearbookId);
        if (total == 0) return;
        long submitted = outlineRepo.countByYearbookIdAndStatus(yearbookId, "submitted");
        int progress = (int) Math.round(submitted * 100.0 / total);
        YbYearbook yb = getById(yearbookId);
        yb.setProgress(progress);
        if (progress == 100) yb.setStatus("completed");
        else if (progress > 0) yb.setStatus("in_progress");
        yearbookRepo.save(yb);
    }
}
