package com.chronicle.repository;

import com.chronicle.entity.YbEntryVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EntryVersionRepository extends JpaRepository<YbEntryVersion, UUID> {
    List<YbEntryVersion> findByEntryIdOrderByVersionDesc(UUID entryId);
    void deleteByEntryId(UUID entryId);
}
