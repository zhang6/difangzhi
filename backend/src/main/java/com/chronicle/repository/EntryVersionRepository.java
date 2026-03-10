package com.chronicle.repository;

import com.chronicle.entity.EntryVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntryVersionRepository extends JpaRepository<EntryVersion, Long> {
    List<EntryVersion> findByEntryIdOrderByVersionDesc(Long entryId);
}
