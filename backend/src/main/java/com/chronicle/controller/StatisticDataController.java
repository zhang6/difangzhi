package com.chronicle.controller;

import com.chronicle.entity.StatisticData;
import com.chronicle.repository.StatisticDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistic-data")
@RequiredArgsConstructor
public class StatisticDataController {

    private final StatisticDataRepository statisticDataRepository;

    @GetMapping
    public List<StatisticData> list(
            @RequestParam(required = false) String indicator,
            @RequestParam(required = false) String region) {
        if (indicator != null && region != null) {
            return statisticDataRepository.findByIndicatorAndRegionOrderByYear(indicator, region);
        }
        return statisticDataRepository.findAll();
    }

    @PostMapping
    public StatisticData create(@RequestBody StatisticData data) {
        return statisticDataRepository.save(data);
    }
}
