package com.chronicle.repository;

import com.chronicle.entity.Material;
import com.chronicle.entity.Material.MaterialType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Page<Material> findByTitleContaining(String title, Pageable pageable);
    Page<Material> findByType(MaterialType type, Pageable pageable);
    Page<Material> findByYear(Integer year, Pageable pageable);
}
