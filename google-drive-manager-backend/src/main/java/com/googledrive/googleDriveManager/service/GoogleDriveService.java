package com.googledrive.googleDriveManager.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.googledrive.googleDriveManager.dto.FileItem;
import com.googledrive.googleDriveManager.model.GoogleToken;
import com.googledrive.googleDriveManager.repository.GoogleTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleDriveService {

    private final GoogleTokenRepository tokenRepository;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String applicationName;
    private final List<String> scopes = Collections.singletonList("https://www.googleapis.com/auth/drive");

    private NetHttpTransport transport;
    private GsonFactory jsonFactory;

    public GoogleDriveService(
            GoogleTokenRepository tokenRepository,
            @Value("${app.google-drive.client-id}") String clientId,
            @Value("${app.google-drive.client-secret}") String clientSecret,
            @Value("${app.google-drive.redirect-uri}") String redirectUri,
            @Value("${app.google-drive.application-name}") String applicationName) {
        this.tokenRepository = tokenRepository;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.applicationName = applicationName;
    }

    private NetHttpTransport getTransport() {
        if (transport == null) {
            try {
                transport = GoogleNetHttpTransport.newTrustedTransport();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize HTTP transport: " + e.getMessage(), e);
            }
        }
        return transport;
    }

    private GsonFactory getJsonFactory() {
        if (jsonFactory == null) {
            jsonFactory = GsonFactory.getDefaultInstance();
        }
        return jsonFactory;
    }

    public boolean isDriveConnected() {
        return tokenRepository.findFirstByOrderByIdAsc().isPresent();
    }

    public String getAuthorizationUrl() {
        GoogleClientSecrets clientSecrets = buildClientSecrets();
        GoogleAuthorizationCodeFlow flow = buildFlow(clientSecrets);
        return flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
    }

    public void storeTokenFromCode(String code) throws IOException {
        GoogleClientSecrets clientSecrets = buildClientSecrets();
        GoogleAuthorizationCodeFlow flow = buildFlow(clientSecrets);
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute();

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(getTransport())
                .setJsonFactory(getJsonFactory())
                .setClientAuthentication(clientSecrets.getInstalled())
                .build()
                .setFromTokenResponse(tokenResponse);

        Drive drive = new Drive.Builder(getTransport(), getJsonFactory(), credential)
                .setApplicationName(applicationName)
                .build();

        String email = drive.about().get().setFields("user/emailAddress").execute().getUser().getEmailAddress();

        String refreshToken = tokenResponse.getRefreshToken();
        if (refreshToken == null) {
            refreshToken = tokenRepository.findFirstByOrderByIdAsc()
                    .map(GoogleToken::getRefreshToken)
                    .orElse(null);
        }

        tokenRepository.deleteAll();
        GoogleToken token = GoogleToken.builder()
                .accountEmail(email)
                .refreshToken(refreshToken)
                .accessToken(tokenResponse.getAccessToken())
                .expiresAtMs(System.currentTimeMillis() + (tokenResponse.getExpiresInSeconds() * 1000))
                .build();
        tokenRepository.save(token);
    }

    private Drive getDriveService() throws IOException {
        GoogleToken token = tokenRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Google Drive is not connected. Please connect your account first."));

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(getTransport())
                .setJsonFactory(getJsonFactory())
                .setClientAuthentication(buildClientSecrets().getInstalled())
                .build();

        credential.setAccessToken(token.getAccessToken());

        if (token.getExpiresAtMs() == null || token.getExpiresAtMs() < System.currentTimeMillis() + 60000) {
            if (token.getRefreshToken() != null) {
                credential.setRefreshToken(token.getRefreshToken());
                credential.refreshToken();
                token.setAccessToken(credential.getAccessToken());
                token.setExpiresAtMs(System.currentTimeMillis() + 3600000L);
                tokenRepository.save(token);
            }
        }

        return new Drive.Builder(getTransport(), getJsonFactory(), credential)
                .setApplicationName(applicationName)
                .build();
    }

    private GoogleClientSecrets buildClientSecrets() {
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
                .setClientId(clientId)
                .setClientSecret(clientSecret);
        return new GoogleClientSecrets().setInstalled(details);
    }

    private GoogleAuthorizationCodeFlow buildFlow(GoogleClientSecrets clientSecrets) {
        return new GoogleAuthorizationCodeFlow.Builder(
                getTransport(), getJsonFactory(), clientSecrets, scopes)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
    }

    public List<FileItem> listFiles(String folderId) throws IOException {
        Drive drive = getDriveService();
        String query = folderId == null || folderId.equals("root")
                ? "'root' in parents and trashed = false"
                : "'" + folderId + "' in parents and trashed = false";
        FileList result = drive.files().list()
                .setQ(query)
                .setFields("files(id,name,mimeType,parents)")
                .execute();
        return result.getFiles().stream()
                .map(this::toFileItem)
                .collect(Collectors.toList());
    }

    public FileItem uploadFile(String folderId, String customName, MultipartFile file) throws IOException {
        Drive drive = getDriveService();
        File fileMetadata = new File();
        fileMetadata.setName(customName != null && !customName.isBlank() ? customName : file.getOriginalFilename());
        if (folderId != null && !folderId.equals("root")) {
            fileMetadata.setParents(Collections.singletonList(folderId));
        }
        com.google.api.client.http.InputStreamContent mediaContent =
                new com.google.api.client.http.InputStreamContent(
                        file.getContentType(),
                        file.getInputStream());
        File uploaded = drive.files()
                .create(fileMetadata, mediaContent)
                .setFields("id,name,mimeType,parents")
                .execute();
        return toFileItem(uploaded);
    }

    public FileItem moveFile(String fileId, String targetFolderId) throws IOException {
        Drive drive = getDriveService();
        File file = drive.files().get(fileId).setFields("parents").execute();
        StringBuilder parentsToRemove = new StringBuilder();
        List<String> previousParents = file.getParents();
        if (previousParents != null && !previousParents.isEmpty()) {
            parentsToRemove.append(String.join(",", previousParents));
        }
        File updated = drive.files()
                .update(fileId, null)
                .setAddParents(targetFolderId)
                .setRemoveParents(parentsToRemove.toString())
                .setFields("id,name,mimeType,parents")
                .execute();
        return toFileItem(updated);
    }

    public FileItem renameFile(String fileId, String newName) throws IOException {
        Drive drive = getDriveService();
        File fileMetadata = new File();
        fileMetadata.setName(newName);
        File updated = drive.files()
                .update(fileId, fileMetadata)
                .setFields("id,name,mimeType,parents")
                .execute();
        return toFileItem(updated);
    }

    public void deleteFile(String fileId) throws IOException {
        Drive drive = getDriveService();
        drive.files().delete(fileId).execute();
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
