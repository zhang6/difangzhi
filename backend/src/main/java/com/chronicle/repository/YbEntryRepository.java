package com.chronicle.repository;

import com.chronicle.entity.YbEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface YbEntryRepository extends JpaRepository<YbEntry, UUID> {
    List<YbEntry> findByOutlineIdOrderBySortOrder(UUID outlineId);
    Page<YbEntry> findByOutlineId(UUID outlineId, Pageable pageable);
    void deleteByOutlineId(UUID outlineId);
}
