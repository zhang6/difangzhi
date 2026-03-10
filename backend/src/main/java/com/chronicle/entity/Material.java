package com.chronicle.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "materials")
@Data
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String source;          // 来源单位
    private Integer year;           // 年份
    private String region;          // 所属地区

    @Enumerated(EnumType.STRING)
    private MaterialType type;      // 资料分类

    @Column(name = "file_path")
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String summary;         // 自动摘要

    @Column(columnDefinition = "TEXT")
    private String keywords;        // 关键词，逗号分隔

    @Column(name = "file_type")
    private String fileType;        // Word, PDF, Excel, 图片等

    @Column(name = "upload_user_id")
    private Long uploadUserId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum MaterialType {
        GOVERNMENT_REPORT,   // 政府报告
        STATISTICS,          // 统计数据
        NEWS,                // 新闻资料
        DEPARTMENT,          // 部门资料
        HISTORY,             // 历史资料
        IMAGE,               // 图片资料
        TABLE_DATA           // 表格数据
    }
}
