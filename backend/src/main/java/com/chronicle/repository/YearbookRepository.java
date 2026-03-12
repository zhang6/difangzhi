package com.chronicle.repository;

import com.chronicle.entity.YbYearbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface YearbookRepository extends JpaRepository<YbYearbook, UUID> {
    @Query("SELECT y FROM YbYearbook y WHERE " +
           "(:keyword IS NULL OR LOWER(y.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR :status = 'all' OR y.status = :status)")
    Page<YbYearbook> findByFilter(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);

    long countByStatus(String status);
}
