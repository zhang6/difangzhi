package com.chronicle.repository;

import com.chronicle.entity.YbHistoryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface HistoryDataRepository extends JpaRepository<YbHistoryData, UUID> {
    @Query("SELECT h FROM YbHistoryData h WHERE " +
           "LOWER(h.outlineSection) LIKE LOWER(CONCAT('%',:keyword,'%')) OR " +
           "LOWER(h.entryTitle) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "ORDER BY h.year DESC")
    List<YbHistoryData> searchByKeyword(@Param("keyword") String keyword);
}
