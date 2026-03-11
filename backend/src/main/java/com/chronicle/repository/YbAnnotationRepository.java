package com.chronicle.repository;

import com.chronicle.entity.YbAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface YbAnnotationRepository extends JpaRepository<YbAnnotation, UUID> {
    List<YbAnnotation> findByEntryIdOrderByCreatedAtDesc(UUID entryId);
    void deleteByEntryId(UUID entryId);
}
