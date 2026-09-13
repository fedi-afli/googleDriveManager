package com.googledrive.googleDriveManager.controller;

import com.googledrive.googleDriveManager.dto.FileItem;
import com.googledrive.googleDriveManager.dto.MoveFileRequest;
import com.googledrive.googleDriveManager.dto.RenameFileRequest;
import com.googledrive.googleDriveManager.service.GoogleDriveService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/drive")
public class DriveController {

    private final GoogleDriveService driveService;

    public DriveController(GoogleDriveService driveService) {
        this.driveService = driveService;
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileItem>> listFiles(@RequestParam(required = false) String folderId) throws IOException {
        return ResponseEntity.ok(driveService.listFiles(folderId));
    }

    @PostMapping("/upload")
    public ResponseEntity<FileItem> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) String folderId,
            @RequestParam(value = "customName", required = false) String customName) throws IOException {
        return ResponseEntity.ok(driveService.uploadFile(folderId, customName, file));
    }

    @PutMapping("/files/{fileId}/move")
    public ResponseEntity<FileItem> moveFile(@PathVariable String fileId, @Valid @RequestBody MoveFileRequest request) throws IOException {
        return ResponseEntity.ok(driveService.moveFile(fileId, request.getTargetFolderId()));
    }

    @PutMapping("/files/{fileId}/rename")
    public ResponseEntity<FileItem> renameFile(@PathVariable String fileId, @Valid @RequestBody RenameFileRequest request) throws IOException {
        return ResponseEntity.ok(driveService.renameFile(fileId, request.getNewName()));
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId) throws IOException {
        driveService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }
}
