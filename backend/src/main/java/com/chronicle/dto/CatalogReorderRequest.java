package com.chronicle.dto;

public record CatalogReorderRequest(Long id, Long parentId, Integer orderNum) {}
