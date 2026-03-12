package com.chronicle.service;

import com.chronicle.entity.YbOutline;
import com.chronicle.repository.OutlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutlineService {

    private final OutlineRepository outlineRepo;

    public List<YbOutline> listByYearbook(UUID yearbookId) {
        return outlineRepo.findByYearbookIdOrderByLevelAscSortOrderAsc(yearbookId);
    }

    public List<YbOutline> listByAssignedUser(UUID userId) {
        return outlineRepo.findByAssignedUserId(userId);
    }

    public YbOutline getById(UUID id) {
        return outlineRepo.findById(id).orElseThrow(() -> new RuntimeException("大纲节点不存在"));
    }

    public YbOutline create(YbOutline outline) {
        return outlineRepo.save(outline);
    }

    @Transactional
    public List<YbOutline> batchCreate(List<YbOutline> outlines) {
        return outlineRepo.saveAll(outlines);
    }

    public YbOutline update(UUID id, YbOutline updates) {
        YbOutline outline = getById(id);
        if (updates.getTitle() != null) outline.setTitle(updates.getTitle());
        if (updates.getUnitName() != null) outline.setUnitName(updates.getUnitName());
        if (updates.getStatus() != null) outline.setStatus(updates.getStatus());
        if (updates.getSortOrder() != null) outline.setSortOrder(updates.getSortOrder());
        if (updates.getAssignedUserId() != null) outline.setAssignedUserId(updates.getAssignedUserId());
        if (updates.getParentId() != null) outline.setParentId(updates.getParentId());
        return outlineRepo.save(outline);
    }

    @Transactional
    public void delete(UUID id) {
        deleteRecursive(id);
    }

    private void deleteRecursive(UUID id) {
        List<YbOutline> children = outlineRepo.findByParentId(id);
        for (YbOutline child : children) {
            deleteRecursive(child.getId());
        }
        outlineRepo.deleteById(id);
    }
}
