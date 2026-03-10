package com.chronicle.service;

import com.chronicle.entity.Entry;
import com.chronicle.entity.EntryVersion;
import com.chronicle.entity.Entry.EntryStatus;
import com.chronicle.repository.EntryRepository;
import com.chronicle.repository.EntryVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final EntryVersionRepository versionRepository;
    private final AiServiceClient aiServiceClient;

    public List<Entry> search(Long catalogId, EntryStatus status) {
        if (catalogId != null && status != null) {
            return entryRepository.findByCatalogId(catalogId).stream()
                .filter(e -> e.getStatus() == status)
                .toList();
        }
        if (catalogId != null) {
            return entryRepository.findByCatalogId(catalogId);
        }
        if (status != null) {
            return entryRepository.findByStatus(status);
        }
        return entryRepository.findAll();
    }

    public Optional<Entry> findById(Long id) {
        return entryRepository.findById(id);
    }

    public Entry save(Entry entry) {
        if (entry.getId() != null) {
            entryRepository.findById(entry.getId()).ifPresent(existing -> {
                if (!existing.getContent().equals(entry.getContent())) {
                    EntryVersion v = new EntryVersion();
                    v.setEntryId(entry.getId());
                    v.setVersion(existing.getVersion());
                    v.setContent(existing.getContent());
                    v.setAuthorId(existing.getAuthorId());
                    versionRepository.save(v);
                    entry.setVersion(existing.getVersion() + 1);
                }
            });
        }
        return entryRepository.save(entry);
    }

    public Entry updateStatus(Long id, EntryStatus status) {
        Entry entry = entryRepository.findById(id).orElseThrow();
        entry.setStatus(status);
        return entryRepository.save(entry);
    }

    public List<EntryVersion> getVersions(Long id) {
        return versionRepository.findByEntryIdOrderByVersionDesc(id);
    }

    public Map<String, String> aiGenerate(Long id) {
        Entry entry = entryRepository.findById(id).orElseThrow();
        String generated = aiServiceClient.generateEntry(entry.getTitle(), entry.getContent());
        entry.setContent(generated);
        entry.setStatus(EntryStatus.IN_PROGRESS);
        entryRepository.save(entry);
        Map<String, String> result = new HashMap<>();
        result.put("content", generated);
        return result;
    }
}
