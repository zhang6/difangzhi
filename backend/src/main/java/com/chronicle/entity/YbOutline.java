package com.chronicle.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "yb_outlines")
@Data
public class YbOutline {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "yearbook_id", nullable = false)
    private UUID yearbookId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer level = 1;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    private String status = "not_started";

    @Column(name = "unit_name")
    private String unitName;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
