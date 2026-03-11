package com.chronicle.repository;

import com.chronicle.entity.YbMaterialFolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface YbMaterialFolderRepository extends JpaRepository<YbMaterialFolder, UUID> {
    Page<YbMaterialFolder> findByUnitNameContainingIgnoreCase(String unitName, Pageable pageable);
}
