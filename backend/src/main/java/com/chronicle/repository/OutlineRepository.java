package com.chronicle.repository;

import com.chronicle.entity.YbOutline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface OutlineRepository extends JpaRepository<YbOutline, UUID> {
    List<YbOutline> findByYearbookIdOrderByLevelAscSortOrderAsc(UUID yearbookId);
    List<YbOutline> findByParentId(UUID parentId);
    List<YbOutline> findByAssignedUserId(UUID userId);
    void deleteByYearbookId(UUID yearbookId);

    @Modifying
    @Query("DELETE FROM YbOutline o WHERE o.id = :id OR o.parentId = :id")
    void deleteWithChildren(@Param("id") UUID id);

    long countByYearbookIdAndStatus(UUID yearbookId, String status);
    long countByYearbookId(UUID yearbookId);
}
