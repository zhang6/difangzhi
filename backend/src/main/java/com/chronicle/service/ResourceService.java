package com.chronicle.service;

import com.chronicle.entity.YbMaterialFile;
import com.chronicle.entity.YbMaterialFolder;
import com.chronicle.repository.YbMaterialFileRepository;
import com.chronicle.repository.YbMaterialFolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final YbMaterialFolderRepository folderRepo;
    private final YbMaterialFileRepository fileRepo;

    public Page<YbMaterialFolder> listFolders(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return folderRepo.findByUnitNameContainingIgnoreCase(keyword, pageable);
        }
        return folderRepo.findAll(pageable);
    }

    public YbMaterialFolder createFolder(YbMaterialFolder folder) {
        return folderRepo.save(folder);
    }

    public YbMaterialFolder updateFolder(UUID id, YbMaterialFolder updates) {
        YbMaterialFolder f = folderRepo.findById(id).orElseThrow();
        if (updates.getUnitName() != null) f.setUnitName(updates.getUnitName());
        if (updates.getTags() != null) f.setTags(updates.getTags());
        return folderRepo.save(f);
    }

    @Transactional
    public void deleteFolder(UUID id) {
        fileRepo.deleteByFolderId(id);
        folderRepo.deleteById(id);
    }

    public Page<YbMaterialFile> listFiles(UUID folderId, Integer year, Pageable pageable) {
        if (year != null && year > 0) {
            return fileRepo.findByFolderIdAndUploadYear(folderId, year, pageable);
        }
        return fileRepo.findByFolderId(folderId, pageable);
    }

    public YbMaterialFile createFile(YbMaterialFile file) {
        YbMaterialFile saved = fileRepo.save(file);
        updateFolderCount(file.getFolderId());
        return saved;
    }

    @Transactional
    public void deleteFile(UUID id) {
        YbMaterialFile file = fileRepo.findById(id).orElseThrow();
        UUID folderId = file.getFolderId();
        fileRepo.deleteById(id);
        updateFolderCount(folderId);
    }

    private void updateFolderCount(UUID folderId) {
        long count = fileRepo.countByFolderId(folderId);
        folderRepo.findById(folderId).ifPresent(f -> {
            f.setFileCount((int) count);
            folderRepo.save(f);
        });
    }
}
