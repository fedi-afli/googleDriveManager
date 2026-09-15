package com.googledrive.googleDriveManager.controller;

import com.googledrive.googleDriveManager.service.GoogleDriveService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {

    private final GoogleDriveService driveService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public OAuthController(GoogleDriveService driveService) {
        this.driveService = driveService;
    }

    // Any authenticated user can check status — read-only, no risk
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getConnectionStatus() {
        return ResponseEntity.ok(Map.of("connected", driveService.isDriveConnected()));
    }

    // Only managers may initiate a connection
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    @GetMapping("/authorize")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl(HttpSession session) {
        // Generate a random state token and bind it to this session.
        // This stops CSRF / authorization-code-injection on the callback.
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        session.setAttribute("oauth_state", state);

        String url = driveService.getAuthorizationUrl(state);
        return ResponseEntity.ok(Map.of("url", url));
    }

    // No @PreAuthorize here — this is a browser redirect from Google and
    // will NOT carry the Authorization header. Validate via session-bound state instead.
    @GetMapping("/callback")
    public ResponseEntity<Void> handleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpSession session) throws IOException {

        String expectedState = (String) session.getAttribute("oauth_state");
        session.removeAttribute("oauth_state"); // one-time use

        if (expectedState == null || state == null || !expectedState.equals(state)) {
            return ResponseEntity.status(302)
                    .header("Location", frontendUrl + "/oauth-callback?status=error&reason=invalid_state")
                    .build();
        }

        driveService.storeTokenFromCode(code);

        return ResponseEntity.status(302)
                .header("Location", frontendUrl + "/oauth-callback?status=success")
                .build();
    }
}