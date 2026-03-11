package com.chronicle.repository;

import com.chronicle.entity.YbHistoryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface YbHistoryDataRepository extends JpaRepository<YbHistoryData, UUID> {
    @Query("SELECT h FROM YbHistoryData h WHERE LOWER(h.entryTitle) LIKE LOWER(CONCAT('%',:kw,'%')) OR LOWER(h.outlineSection) LIKE LOWER(CONCAT('%',:kw,'%')) ORDER BY h.year DESC")
    List<YbHistoryData> searchByKeyword(String kw);
}
