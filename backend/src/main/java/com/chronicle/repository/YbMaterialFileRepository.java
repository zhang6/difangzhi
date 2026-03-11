package com.chronicle.repository;

import com.chronicle.entity.YbMaterialFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface YbMaterialFileRepository extends JpaRepository<YbMaterialFile, UUID> {
    Page<YbMaterialFile> findByFolderId(UUID folderId, Pageable pageable);
    Page<YbMaterialFile> findByFolderIdAndUploadYear(UUID folderId, Integer year, Pageable pageable);
    long countByFolderId(UUID folderId);
    void deleteByFolderId(UUID folderId);
}
