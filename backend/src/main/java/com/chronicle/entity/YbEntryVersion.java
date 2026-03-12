package com.chronicle.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "yb_entry_versions")
@Data
public class YbEntryVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entry_id", nullable = false)
    private UUID entryId;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "revision_note", length = 500)
    private String revisionNote;

    @Column(name = "editor_id")
    private UUID editorId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "editor_id", insertable = false, updatable = false)
    private YbUser editor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
