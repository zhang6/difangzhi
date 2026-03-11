package com.chronicle.service;

import com.chronicle.entity.YbYearbook;
import com.chronicle.entity.YbYearbookManager;
import com.chronicle.repository.YbYearbookManagerRepository;
import com.chronicle.repository.YbYearbookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class YearbookService {

    private final YbYearbookRepository yearbookRepo;
    private final YbYearbookManagerRepository managerRepo;

    public Page<YbYearbook> list(String keyword, String status, Pageable pageable) {
        boolean hasKw = keyword != null && !keyword.isBlank();
        boolean hasSt = status != null && !status.isBlank() && !"all".equals(status);
        if (hasKw && hasSt) return yearbookRepo.findByNameContainingIgnoreCaseAndStatus(keyword, status, pageable);
        if (hasKw) return yearbookRepo.findByNameContainingIgnoreCase(keyword, pageable);
        if (hasSt) return yearbookRepo.findByStatus(status, pageable);
        return yearbookRepo.findAll(pageable);
    }

    public YbYearbook getById(UUID id) {
        return yearbookRepo.findById(id).orElseThrow();
    }

    public YbYearbook create(YbYearbook yb) {
        return yearbookRepo.save(yb);
    }

    public YbYearbook update(UUID id, YbYearbook updates) {
        YbYearbook yb = yearbookRepo.findById(id).orElseThrow();
        if (updates.getName() != null) yb.setName(updates.getName());
        if (updates.getStartDate() != null) yb.setStartDate(updates.getStartDate());
        if (updates.getEndDate() != null) yb.setEndDate(updates.getEndDate());
        if (updates.getCoverType() != null) yb.setCoverType(updates.getCoverType());
        if (updates.getStatus() != null) yb.setStatus(updates.getStatus());
        if (updates.getProgress() != null) yb.setProgress(updates.getProgress());
        return yearbookRepo.save(yb);
    }

    @Transactional
    public void delete(UUID id) {
        managerRepo.deleteByYearbookId(id);
        yearbookRepo.deleteById(id);
    }

    public List<YbYearbookManager> getManagers(UUID yearbookId) {
        return managerRepo.findByYearbookId(yearbookId);
    }

    @Transactional
    public YbYearbookManager setManager(UUID yearbookId, UUID userId) {
        managerRepo.deleteByYearbookId(yearbookId);
        YbYearbookManager m = new YbYearbookManager();
        m.setYearbookId(yearbookId);
        m.setUserId(userId);
        return managerRepo.save(m);
    }
}
