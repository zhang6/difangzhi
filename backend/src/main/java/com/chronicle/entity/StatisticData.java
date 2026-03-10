package com.chronicle.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "statistic_data")
@Data
public class StatisticData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String indicator;    // 指标名称

    private Integer year;
    private String region;      // 地区

    @Column(precision = 20, scale = 4)
    private BigDecimal value;

    private String unit;        // 单位

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
