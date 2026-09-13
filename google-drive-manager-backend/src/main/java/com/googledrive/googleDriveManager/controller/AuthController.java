package com.googledrive.googleDriveManager.controller;

import com.googledrive.googleDriveManager.dto.LoginRequest;
import com.googledrive.googleDriveManager.dto.LoginResponse;
import com.googledrive.googleDriveManager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
