package com.chronicle.service;

import com.chronicle.entity.YbMaterialFile;
import com.chronicle.entity.YbMaterialFolder;
import com.chronicle.repository.MaterialFileRepository;
import com.chronicle.repository.MaterialFolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MaterialFolderRepository folderRepo;
    private final MaterialFileRepository fileRepo;

    public Page<YbMaterialFolder> listFolders(String keyword, Pageable pageable) {
        String kw = (keyword != null && keyword.isBlank()) ? null : keyword;
        return folderRepo.findByKeyword(kw, pageable);
    }

    public YbMaterialFolder getFolder(UUID id) {
        return folderRepo.findById(id).orElseThrow(() -> new RuntimeException("文件夹不存在"));
    }

    public YbMaterialFolder createFolder(YbMaterialFolder folder) {
        if (folderRepo.existsByUnitName(folder.getUnitName())) {
            throw new RuntimeException("供稿单位名称已存在");
        }
        return folderRepo.save(folder);
    }

    public YbMaterialFolder updateFolder(UUID id, YbMaterialFolder updates) {
        YbMaterialFolder folder = getFolder(id);
        if (updates.getUnitName() != null && !updates.getUnitName().equals(folder.getUnitName())) {
            if (folderRepo.existsByUnitName(updates.getUnitName())) {
                throw new RuntimeException("供稿单位名称已存在");
            }
            folder.setUnitName(updates.getUnitName());
        }
        if (updates.getTags() != null) folder.setTags(updates.getTags());
        return folderRepo.save(folder);
    }

    @Transactional
    public void deleteFolder(UUID id) {
        fileRepo.deleteByFolderId(id);
        folderRepo.deleteById(id);
    }

    public Page<YbMaterialFile> listFiles(UUID folderId, Integer year, Pageable pageable) {
        return fileRepo.findByFolderIdAndYear(folderId, year, pageable);
    }

    public YbMaterialFile createFile(YbMaterialFile file) {
        YbMaterialFile saved = fileRepo.save(file);
        updateFileCount(file.getFolderId());
        return saved;
    }

    public void deleteFile(UUID id) {
        YbMaterialFile file = fileRepo.findById(id).orElseThrow(() -> new RuntimeException("文件不存在"));
        fileRepo.deleteById(id);
        updateFileCount(file.getFolderId());
    }

    private void updateFileCount(UUID folderId) {
        folderRepo.findById(folderId).ifPresent(folder -> {
            folder.setFileCount((int) fileRepo.countByFolderId(folderId));
            folderRepo.save(folder);
        });
    }

    public List<YbMaterialFile> listFilesByOutline(UUID outlineId) {
        return fileRepo.findByOutlineId(outlineId);
    }
}
