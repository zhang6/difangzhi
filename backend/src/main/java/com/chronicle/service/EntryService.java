package com.chronicle.service;

import com.chronicle.entity.*;
import com.chronicle.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final YbEntryRepository entryRepo;
    private final YbEntryVersionRepository versionRepo;
    private final YbAnnotationRepository annotationRepo;
    private final YbHistoryDataRepository historyRepo;

    public List<YbEntry> listByOutline(UUID outlineId) {
        return entryRepo.findByOutlineIdOrderBySortOrder(outlineId);
    }

    public Page<YbEntry> listByOutlinePaged(UUID outlineId, Pageable pageable) {
        return entryRepo.findByOutlineId(outlineId, pageable);
    }

    public YbEntry getById(UUID id) {
        return entryRepo.findById(id).orElseThrow();
    }

    public YbEntry create(YbEntry entry) {
        return entryRepo.save(entry);
    }

    public YbEntry update(UUID id, YbEntry updates) {
        YbEntry e = entryRepo.findById(id).orElseThrow();
        if (updates.getTitle() != null) e.setTitle(updates.getTitle());
        if (updates.getOriginalContent() != null) e.setOriginalContent(updates.getOriginalContent());
        if (updates.getAiContent() != null) e.setAiContent(updates.getAiContent());
        if (updates.getStatus() != null) e.setStatus(updates.getStatus());
        if (updates.getSortOrder() != null) e.setSortOrder(updates.getSortOrder());
        return entryRepo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        versionRepo.deleteByEntryId(id);
        annotationRepo.deleteByEntryId(id);
        entryRepo.deleteById(id);
    }

    public List<YbEntryVersion> getVersions(UUID entryId) {
        return versionRepo.findByEntryIdOrderByVersionDesc(entryId);
    }

    public YbEntryVersion createVersion(YbEntryVersion version) {
        return versionRepo.save(version);
    }

    public List<YbAnnotation> getAnnotations(UUID entryId) {
        return annotationRepo.findByEntryIdOrderByCreatedAtDesc(entryId);
    }

    public YbAnnotation createAnnotation(YbAnnotation annotation) {
        return annotationRepo.save(annotation);
    }

    public YbAnnotation updateAnnotationStatus(UUID id, String status) {
        YbAnnotation a = annotationRepo.findById(id).orElseThrow();
        a.setProcessStatus(status);
        return annotationRepo.save(a);
    }

    public List<YbHistoryData> searchHistory(String keyword) {
        return historyRepo.searchByKeyword(keyword);
    }
}
