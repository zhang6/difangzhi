package com.chronicle.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "yb_history_data")
@Data
public class YbHistoryData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "yearbook_name")
    private String yearbookName;

    @Column(name = "outline_section")
    private String outlineSection;

    @Column(name = "entry_title")
    private String entryTitle;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer year;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
