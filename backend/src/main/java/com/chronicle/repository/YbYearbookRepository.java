package com.chronicle.repository;

import com.chronicle.entity.YbYearbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface YbYearbookRepository extends JpaRepository<YbYearbook, UUID> {
    Page<YbYearbook> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<YbYearbook> findByStatus(String status, Pageable pageable);
    Page<YbYearbook> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);
}
