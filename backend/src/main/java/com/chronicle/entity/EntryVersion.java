package com.chronicle.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "entry_versions")
@Data
public class EntryVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_id", nullable = false)
    private Long entryId;

    private Integer version;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
