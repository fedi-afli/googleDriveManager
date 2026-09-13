package com.googledrive.googleDriveManager.controller;

import com.googledrive.googleDriveManager.service.GoogleDriveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {

    private final GoogleDriveService driveService;

    public OAuthController(GoogleDriveService driveService) {
        this.driveService = driveService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getConnectionStatus() {
        return ResponseEntity.ok(Map.of("connected", driveService.isDriveConnected()));
    }

    @GetMapping("/authorize")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl() {
        return ResponseEntity.ok(Map.of("url", driveService.getAuthorizationUrl()));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> handleCallback(@RequestParam("code") String code) throws IOException {
        driveService.storeTokenFromCode(code);
        return ResponseEntity.status(302)
                .header("Location", "http://localhost:4200/oauth-callback?status=success")
                .build();
    }
}
