package com.chronicle.controller;

import com.chronicle.dto.PageResult;
import com.chronicle.entity.YbMaterialFile;
import com.chronicle.entity.YbMaterialFolder;
import com.chronicle.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping("/folders")
    public PageResult<YbMaterialFolder> listFolders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String keyword) {
        var pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());
        return PageResult.of(resourceService.listFolders(keyword, pageable));
    }

    @PostMapping("/folders")
    public YbMaterialFolder createFolder(@RequestBody YbMaterialFolder folder) {
        return resourceService.createFolder(folder);
    }

    @PutMapping("/folders/{id}")
    public YbMaterialFolder updateFolder(@PathVariable UUID id, @RequestBody YbMaterialFolder folder) {
        return resourceService.updateFolder(id, folder);
    }

    @DeleteMapping("/folders/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable UUID id) {
        resourceService.deleteFolder(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/files")
    public PageResult<YbMaterialFile> listFiles(
            @RequestParam UUID folderId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) Integer year) {
        var pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());
        return PageResult.of(resourceService.listFiles(folderId, year, pageable));
    }

    @PostMapping("/files")
    public YbMaterialFile createFile(@RequestBody YbMaterialFile file) {
        return resourceService.createFile(file);
    }

    @DeleteMapping("/files/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        resourceService.deleteFile(id);
        return ResponseEntity.ok().build();
    }
}
