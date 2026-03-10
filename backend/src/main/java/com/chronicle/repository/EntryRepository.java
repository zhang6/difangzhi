package com.chronicle.repository;

import com.chronicle.entity.Entry;
import com.chronicle.entity.Entry.EntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntryRepository extends JpaRepository<Entry, Long> {
    List<Entry> findByCatalogId(Long catalogId);
    List<Entry> findByStatus(EntryStatus status);
}
