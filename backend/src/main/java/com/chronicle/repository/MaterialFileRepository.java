package com.chronicle.repository;

import com.chronicle.entity.YbMaterialFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface MaterialFileRepository extends JpaRepository<YbMaterialFile, UUID> {
    @Query("SELECT f FROM YbMaterialFile f WHERE f.folderId = :folderId AND (:year IS NULL OR f.uploadYear = :year) ORDER BY f.createdAt DESC")
    Page<YbMaterialFile> findByFolderIdAndYear(@Param("folderId") UUID folderId, @Param("year") Integer year, Pageable pageable);

    List<YbMaterialFile> findByOutlineId(UUID outlineId);
    void deleteByFolderId(UUID folderId);
    long countByFolderId(UUID folderId);
}
