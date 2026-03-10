package com.chronicle.repository;

import com.chronicle.entity.StatisticData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatisticDataRepository extends JpaRepository<StatisticData, Long> {
    List<StatisticData> findByIndicatorAndRegionOrderByYear(String indicator, String region);
}
