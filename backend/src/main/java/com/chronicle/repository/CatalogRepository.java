package com.chronicle.repository;

import com.chronicle.entity.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogRepository extends JpaRepository<Catalog, Long> {
    List<Catalog> findByParentIdOrderByOrderNum(Long parentId);
    List<Catalog> findByParentIdIsNullOrderByOrderNum();
    List<Catalog> findAllByOrderByOrderNum();
}
