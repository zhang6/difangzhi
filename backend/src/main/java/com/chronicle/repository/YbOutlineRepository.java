package com.chronicle.repository;

import com.chronicle.entity.YbOutline;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface YbOutlineRepository extends JpaRepository<YbOutline, UUID> {
    List<YbOutline> findByYearbookIdOrderBySortOrder(UUID yearbookId);
    List<YbOutline> findByAssignedUserId(UUID userId);
    void deleteByYearbookId(UUID yearbookId);
    void deleteByParentId(UUID parentId);
}
