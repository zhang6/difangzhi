package com.chronicle.service;

import com.chronicle.entity.Catalog;
import com.chronicle.entity.Entry;
import com.chronicle.repository.CatalogRepository;
import com.chronicle.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublishService {

    private final CatalogRepository catalogRepository;
    private final EntryRepository entryRepository;

    public byte[] export(String format, List<Long> catalogIds, Long entryId) {
        StringBuilder sb = new StringBuilder();
        sb.append("# 地方志\n\n");

        if (entryId != null) {
            entryRepository.findById(entryId).ifPresent(e -> {
                sb.append("## ").append(e.getTitle()).append("\n\n");
                sb.append(e.getContent() != null ? e.getContent() : "").append("\n\n");
            });
        } else {
            List<Catalog> catalogs = catalogIds != null && !catalogIds.isEmpty()
                ? catalogIds.stream().map(catalogRepository::findById).filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get).toList()
                : catalogRepository.findByParentIdIsNullOrderByOrderNum();
            appendCatalogContent(sb, catalogs, 0);
        }

        String content = sb.toString();
        return switch (format.toLowerCase()) {
            case "markdown", "md" -> content.getBytes();
            case "html" -> generateHtml(content).getBytes();
            default -> content.getBytes(); // Word/PDF 需额外库，此处简化
        };
    }

    private void appendCatalogContent(StringBuilder sb, List<Catalog> catalogs, int level) {
        for (Catalog c : catalogs) {
            sb.append("#".repeat(level + 1)).append(" ").append(c.getTitle()).append("\n\n");
            List<Entry> entries = entryRepository.findByCatalogId(c.getId());
            for (Entry e : entries) {
                sb.append("## ").append(e.getTitle()).append("\n\n");
                sb.append(e.getContent() != null ? e.getContent() : "").append("\n\n");
            }
            List<Catalog> children = catalogRepository.findByParentIdOrderByOrderNum(c.getId());
            if (!children.isEmpty()) {
                appendCatalogContent(sb, children, level + 1);
            }
        }
    }

    private String generateHtml(String markdown) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>地方志</title></head>
            <body><pre>%s</pre></body>
            </html>
            """.formatted(markdown.replace("<", "&lt;").replace(">", "&gt;"));
    }
}
