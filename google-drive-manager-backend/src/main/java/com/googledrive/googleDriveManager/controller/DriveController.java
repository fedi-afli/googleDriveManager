package com.googledrive.googleDriveManager.controller;

import com.googledrive.googleDriveManager.dto.FileItem;
import com.googledrive.googleDriveManager.dto.MoveFileRequest;
import com.googledrive.googleDriveManager.dto.RenameFileRequest;
import com.googledrive.googleDriveManager.model.User;
import com.googledrive.googleDriveManager.model.enums.Role;
import com.googledrive.googleDriveManager.repository.UserRepository;
import com.googledrive.googleDriveManager.service.BrandService;
import com.googledrive.googleDriveManager.service.GoogleDriveService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/drive")
public class DriveController {

    private final GoogleDriveService driveService;
    private final BrandService brandService;
    private final UserRepository userRepository;

    public DriveController(GoogleDriveService driveService,
                           BrandService brandService,
                           UserRepository userRepository) {
        this.driveService = driveService;
        this.brandService = brandService;
        this.userRepository = userRepository;
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileItem>> listFiles(
            @RequestParam(required = false) String folderId,
            Authentication authentication) throws IOException {
        User user = getUser(authentication);
        validateFolderAccess(user, folderId);
        return ResponseEntity.ok(driveService.listFiles(folderId));
    }

    @PostMapping("/upload")
    public ResponseEntity<FileItem> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) String folderId,
            @RequestParam(value = "customName", required = false) String customName,
            Authentication authentication) throws IOException {
        User user = getUser(authentication);
        validateFolderAccess(user, folderId);
        return ResponseEntity.ok(driveService.uploadFile(folderId, customName, file));
    }

    @PutMapping("/files/{fileId}/move")
    public ResponseEntity<FileItem> moveFile(
            @PathVariable String fileId,
            @Valid @RequestBody MoveFileRequest request,
            Authentication authentication) throws IOException {
        User user = getUser(authentication);
        validateFolderAccess(user, request.getTargetFolderId());
        return ResponseEntity.ok(driveService.moveFile(fileId, request.getTargetFolderId()));
    }

    @PutMapping("/files/{fileId}/rename")
    public ResponseEntity<FileItem> renameFile(
            @PathVariable String fileId,
            @Valid @RequestBody RenameFileRequest request,
            Authentication authentication) throws IOException {
        return ResponseEntity.ok(driveService.renameFile(fileId, request.getNewName()));
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String fileId,
            Authentication authentication) throws IOException {
        User user = getUser(authentication);
        if (user.getRole() != Role.TEAM_MANAGER) {
            throw new RuntimeException("Only team managers can delete files");
        }
        driveService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    private void validateFolderAccess(User user, String folderId) {
        if (user.getRole() == Role.TEAM_MANAGER) {
            return;
        }
        if (folderId == null || folderId.equals("root")) {
            throw new RuntimeException("Access denied: you can only access your assigned brand folders");
        }
        if (!brandService.canAccessFolder(user, folderId)) {
            throw new RuntimeException("Access denied: you do not have access to this folder");
        }
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
