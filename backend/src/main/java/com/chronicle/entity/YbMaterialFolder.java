package com.chronicle.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "yb_material_folders")
@Data
public class YbMaterialFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "unit_name", nullable = false, unique = true, length = 100)
    private String unitName;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(name = "file_count", nullable = false)
    private Integer fileCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
