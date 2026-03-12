package com.chronicle.repository;

import com.chronicle.entity.YbMaterialFolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface MaterialFolderRepository extends JpaRepository<YbMaterialFolder, UUID> {
    Optional<YbMaterialFolder> findByUnitName(String unitName);
    boolean existsByUnitName(String unitName);

    @Query("SELECT f FROM YbMaterialFolder f WHERE " +
           ":keyword IS NULL OR LOWER(f.unitName) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(f.tags) LIKE LOWER(CONCAT('%',:keyword,'%'))")
    Page<YbMaterialFolder> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
