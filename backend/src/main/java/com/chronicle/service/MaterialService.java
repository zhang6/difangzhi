package com.chronicle.service;

import com.chronicle.entity.Material;
import com.chronicle.entity.Material.MaterialType;
import com.chronicle.repository.MaterialRepository;
import com.chronicle.service.storage.MinioStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MinioStorageService storageService;
    private final AiServiceClient aiServiceClient;

    public Page<Material> search(String title, MaterialType type, Integer year, Pageable pageable) {
        if (title != null && !title.isBlank()) {
            return materialRepository.findByTitleContaining(title, pageable);
        }
        if (type != null) {
            return materialRepository.findByType(type, pageable);
        }
        if (year != null) {
            return materialRepository.findByYear(year, pageable);
        }
        return materialRepository.findAll(pageable);
    }

    public Optional<Material> findById(Long id) {
        return materialRepository.findById(id);
    }

    public Material save(Material material) {
        return materialRepository.save(material);
    }

    public Material upload(MultipartFile file, String title, String source, Integer year, MaterialType type) {
        String filePath = storageService.upload(file, "materials");
        Material material = new Material();
        material.setTitle(title != null ? title : file.getOriginalFilename());
        material.setSource(source);
        material.setYear(year);
        material.setType(type);
        material.setFilePath(filePath);
        material.setFileType(getFileType(file.getOriginalFilename()));
        return materialRepository.save(material);
    }

    public void deleteById(Long id) {
        materialRepository.findById(id).ifPresent(m -> {
            if (m.getFilePath() != null) {
                storageService.delete(m.getFilePath());
            }
            materialRepository.deleteById(id);
        });
    }

    public Map<String, String> generateSummary(Long id, int length) {
        Material material = materialRepository.findById(id).orElseThrow();
        String content = material.getContent();
        if (content == null || content.isBlank()) {
            content = "（文档内容待解析）";
        }
        String summary = aiServiceClient.summarize(content, length);
        material.setSummary(summary);
        materialRepository.save(material);
        Map<String, String> result = new HashMap<>();
        result.put("summary", summary);
        return result;
    }

    private String getFileType(String filename) {
        if (filename == null) return "unknown";
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf(".") + 1).toLowerCase() : "";
        return switch (ext) {
            case "doc", "docx" -> "Word";
            case "pdf" -> "PDF";
            case "xls", "xlsx" -> "Excel";
            case "jpg", "jpeg", "png", "gif" -> "图片";
            default -> ext.isEmpty() ? "unknown" : ext;
        };
    }
}
