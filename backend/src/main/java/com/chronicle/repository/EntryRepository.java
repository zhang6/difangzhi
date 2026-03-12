package com.chronicle.repository;

import com.chronicle.entity.YbEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EntryRepository extends JpaRepository<YbEntry, UUID> {
    Page<YbEntry> findByOutlineIdOrderBySortOrderAsc(UUID outlineId, Pageable pageable);
    List<YbEntry> findByOutlineIdOrderBySortOrderAsc(UUID outlineId);
    void deleteByOutlineId(UUID outlineId);
}
