package com.chronicle.service;

import com.chronicle.entity.Catalog;
import com.chronicle.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogRepository catalogRepository;

    public List<Catalog> getTree(Long parentId) {
        if (parentId == null) {
            return catalogRepository.findByParentIdIsNullOrderByOrderNum();
        }
        return catalogRepository.findByParentIdOrderByOrderNum(parentId);
    }

    public List<Catalog> getFullTree() {
        return catalogRepository.findAllByOrderByOrderNum();
    }

    public Optional<Catalog> findById(Long id) {
        return catalogRepository.findById(id);
    }

    public Catalog save(Catalog catalog) {
        if (catalog.getOrderNum() == null) {
            List<Catalog> siblings = catalog.getParentId() == null
                ? catalogRepository.findByParentIdIsNullOrderByOrderNum()
                : catalogRepository.findByParentIdOrderByOrderNum(catalog.getParentId());
            catalog.setOrderNum(siblings.size() + 1);
        }
        return catalogRepository.save(catalog);
    }

    @Transactional
    public void reorder(List<com.chronicle.dto.CatalogReorderRequest> items) {
        for (var req : items) {
            catalogRepository.findById(req.id()).ifPresent(c -> {
                c.setParentId(req.parentId());
                c.setOrderNum(req.orderNum());
                catalogRepository.save(c);
            });
        }
    }

    public void deleteById(Long id) {
        catalogRepository.deleteById(id);
    }
}
