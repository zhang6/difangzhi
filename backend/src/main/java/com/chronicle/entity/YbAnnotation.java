package com.chronicle.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "yb_annotations")
@Data
public class YbAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entry_id", nullable = false)
    private UUID entryId;

    @Column(nullable = false, length = 200)
    private String content;

    @Column(name = "author_id")
    private UUID authorId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private YbUser author;

    @Column(name = "annotation_type", length = 20)
    private String annotationType = "comment";

    @Column(name = "process_status", length = 20)
    private String processStatus = "pending";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
