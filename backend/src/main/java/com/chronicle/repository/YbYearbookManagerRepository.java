package com.chronicle.repository;

import com.chronicle.entity.YbYearbookManager;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface YbYearbookManagerRepository extends JpaRepository<YbYearbookManager, UUID> {
    List<YbYearbookManager> findByYearbookId(UUID yearbookId);
    void deleteByYearbookId(UUID yearbookId);
}
