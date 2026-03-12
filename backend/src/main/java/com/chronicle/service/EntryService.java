package com.chronicle.service;

import com.chronicle.entity.YbAnnotation;
import com.chronicle.entity.YbEntry;
import com.chronicle.entity.YbEntryVersion;
import com.chronicle.entity.YbHistoryData;
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

    private final EntryRepository entryRepo;
    private final EntryVersionRepository versionRepo;
    private final AnnotationRepository annotationRepo;
    private final HistoryDataRepository historyRepo;

    public Page<YbEntry> listByOutline(UUID outlineId, Pageable pageable) {
        return entryRepo.findByOutlineIdOrderBySortOrderAsc(outlineId, pageable);
    }

    public YbEntry getById(UUID id) {
        return entryRepo.findById(id).orElseThrow(() -> new RuntimeException("条目不存在"));
    }

    public YbEntry create(YbEntry entry) {
        return entryRepo.save(entry);
    }

    public YbEntry update(UUID id, YbEntry updates) {
        YbEntry entry = getById(id);
        if (updates.getTitle() != null) entry.setTitle(updates.getTitle());
        if (updates.getOriginalContent() != null) entry.setOriginalContent(updates.getOriginalContent());
        if (updates.getAiContent() != null) entry.setAiContent(updates.getAiContent());
        if (updates.getStatus() != null) entry.setStatus(updates.getStatus());
        if (updates.getSortOrder() != null) entry.setSortOrder(updates.getSortOrder());
        return entryRepo.save(entry);
    }

    @Transactional
    public void delete(UUID id) {
        versionRepo.deleteByEntryId(id);
        annotationRepo.deleteByEntryId(id);
        entryRepo.deleteById(id);
    }

    public List<YbEntryVersion> listVersions(UUID entryId) {
        return versionRepo.findByEntryIdOrderByVersionDesc(entryId);
    }

    public YbEntryVersion createVersion(YbEntryVersion version) {
        return versionRepo.save(version);
    }

    public void deleteVersion(UUID versionId) {
        versionRepo.deleteById(versionId);
    }

    public List<YbAnnotation> listAnnotations(UUID entryId) {
        return annotationRepo.findByEntryIdOrderByCreatedAtDesc(entryId);
    }

    public YbAnnotation createAnnotation(YbAnnotation annotation) {
        return annotationRepo.save(annotation);
    }

    public YbAnnotation updateAnnotation(UUID id, YbAnnotation updates) {
        YbAnnotation ann = annotationRepo.findById(id).orElseThrow(() -> new RuntimeException("批注不存在"));
        if (updates.getContent() != null) ann.setContent(updates.getContent());
        if (updates.getProcessStatus() != null) ann.setProcessStatus(updates.getProcessStatus());
        return annotationRepo.save(ann);
    }

    public void deleteAnnotation(UUID id) {
        annotationRepo.deleteById(id);
    }

    public List<YbHistoryData> searchHistory(String keyword) {
        return historyRepo.searchByKeyword(keyword);
    }
}
