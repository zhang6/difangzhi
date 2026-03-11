package com.chronicle.service;

import com.chronicle.entity.YbOutline;
import com.chronicle.repository.YbOutlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutlineService {

    private final YbOutlineRepository outlineRepo;

    public List<YbOutline> listByYearbook(UUID yearbookId) {
        return outlineRepo.findByYearbookIdOrderBySortOrder(yearbookId);
    }

    public List<YbOutline> listByAssignedUser(UUID userId) {
        return outlineRepo.findByAssignedUserId(userId);
    }

    public YbOutline create(YbOutline outline) {
        return outlineRepo.save(outline);
    }

    public List<YbOutline> batchCreate(List<YbOutline> outlines) {
        return outlineRepo.saveAll(outlines);
    }

    public YbOutline update(UUID id, YbOutline updates) {
        YbOutline o = outlineRepo.findById(id).orElseThrow();
        if (updates.getTitle() != null) o.setTitle(updates.getTitle());
        if (updates.getUnitName() != null) o.setUnitName(updates.getUnitName());
        if (updates.getStatus() != null) o.setStatus(updates.getStatus());
        if (updates.getAssignedUserId() != null) o.setAssignedUserId(updates.getAssignedUserId());
        if (updates.getSortOrder() != null) o.setSortOrder(updates.getSortOrder());
        return outlineRepo.save(o);
    }

    @Transactional
    public void delete(UUID id) {
        outlineRepo.deleteByParentId(id);
        outlineRepo.deleteById(id);
    }
}
