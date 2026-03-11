package com.chronicle.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "yb_material_files")
@Data
public class YbMaterialFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "folder_id", nullable = false)
    private UUID folderId;

    @Column(name = "outline_id")
    private UUID outlineId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize = 0L;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "upload_year")
    private Integer uploadYear;

    private String source = "upload";

    @Column(name = "uploaded_by")
    private UUID uploadedBy;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
