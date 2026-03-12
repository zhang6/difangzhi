package com.chronicle.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "yb_history_data")
@Data
public class YbHistoryData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "yearbook_name", length = 100)
    private String yearbookName;

    @Column(name = "outline_section", length = 200)
    private String outlineSection;

    @Column(name = "entry_title", length = 200)
    private String entryTitle;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer year;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
