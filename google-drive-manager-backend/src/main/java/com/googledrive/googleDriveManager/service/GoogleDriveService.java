package com.googledrive.googleDriveManager.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential.Builder;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.googledrive.googleDriveManager.dto.FileItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleDriveService {

    private final Drive driveService;

    public GoogleDriveService(@Value("${app.google-drive.application-name}") String appName) {
        this.driveService = initializeDrive(appName);
    }

    private Drive initializeDrive(String appName) {
        try {
            InputStream credentialsStream = new ClassPathResource("google-drive-service-account.json").getInputStream();
            NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredential credential = GoogleCredential.fromStream(credentialsStream)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/drive"));
            return new Drive.Builder(transport, GsonFactory.getDefaultInstance(), credential)
                    .setApplicationName(appName)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Google Drive service: " + e.getMessage(), e);
        }
    }

    public List<FileItem> listFiles(String folderId) throws IOException {
        String query = folderId == null || folderId.equals("root")
                ? "'root' in parents and trashed = false"
                : "'" + folderId + "' in parents and trashed = false";
        FileList result = driveService.files().list()
                .setQ(query)
                .setFields("files(id,name,mimeType,parents)")
                .execute();
        return result.getFiles().stream()
                .map(this::toFileItem)
                .collect(Collectors.toList());
    }

    public FileItem uploadFile(String folderId, String customName, MultipartFile file) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(customName != null && !customName.isBlank() ? customName : file.getOriginalFilename());
        if (folderId != null && !folderId.equals("root")) {
            fileMetadata.setParents(Collections.singletonList(folderId));
        }
        com.google.api.client.http.InputStreamContent mediaContent =
                new com.google.api.client.http.InputStreamContent(
                        file.getContentType(),
                        file.getInputStream());
        File uploaded = driveService.files()
                .create(fileMetadata, mediaContent)
                .setFields("id,name,mimeType,parents")
                .execute();
        return toFileItem(uploaded);
    }

    public FileItem moveFile(String fileId, String targetFolderId) throws IOException {
        File file = driveService.files().get(fileId).setFields("parents").execute();
        StringBuilder parentsToRemove = new StringBuilder();
        List<String> previousParents = file.getParents();
        if (previousParents != null && !previousParents.isEmpty()) {
            parentsToRemove.append(String.join(",", previousParents));
        }
        File updated = driveService.files()
                .update(fileId, null)
                .setAddParents(targetFolderId)
                .setRemoveParents(parentsToRemove.toString())
                .setFields("id,name,mimeType,parents")
                .execute();
        return toFileItem(updated);
    }

    public FileItem renameFile(String fileId, String newName) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(newName);
        File updated = driveService.files()
                .update(fileId, fileMetadata)
                .setFields("id,name,mimeType,parents")
                .execute();
        return toFileItem(updated);
    }

    public void deleteFile(String fileId) throws IOException {
        driveService.files().delete(fileId).execute();
    }

    private FileItem toFileItem(File file) {
        return FileItem.builder()
                .id(file.getId())
                .name(file.getName())
                .mimeType(file.getMimeType())
                .folder("application/vnd.google-apps.folder".equals(file.getMimeType()))
                .parents(file.getParents())
                .build();
    }
}
