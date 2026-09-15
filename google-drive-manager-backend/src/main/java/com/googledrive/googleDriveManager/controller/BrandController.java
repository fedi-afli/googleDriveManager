package com.googledrive.googleDriveManager.controller;

import com.googledrive.googleDriveManager.dto.AssignMemberRequest;
import com.googledrive.googleDriveManager.dto.BrandResponse;
import com.googledrive.googleDriveManager.dto.CreateBrandRequest;
import com.googledrive.googleDriveManager.dto.UserResponse;
import com.googledrive.googleDriveManager.model.User;
import com.googledrive.googleDriveManager.model.enums.Role;
import com.googledrive.googleDriveManager.repository.UserRepository;
import com.googledrive.googleDriveManager.service.BrandService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;
    private final UserRepository userRepository;

    public BrandController(BrandService brandService, UserRepository userRepository) {
        this.brandService = brandService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('TEAM_MANAGER')")
    @PostMapping
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody CreateBrandRequest request) throws IOException {
        return ResponseEntity.ok(brandService.createBrand(request));
    }

    @GetMapping
    public ResponseEntity<List<BrandResponse>> getBrandsForUser(Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(brandService.getBrandsForUser(user).stream()
                .map(brandService::toBrandResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{brandId}")
    public ResponseEntity<BrandResponse> getBrand(@PathVariable Long brandId, Authentication authentication) {
        User user = getUser(authentication);
        if (user.getRole() != Role.TEAM_MANAGER) {
            brandService.getBrandsForUser(user).stream()
                    .filter(b -> b.getId().equals(brandId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("You do not have access to this brand"));
        }
        return ResponseEntity.ok(brandService.getBrand(brandId));
    }

    @PreAuthorize("hasRole('TEAM_MANAGER')")
    @PostMapping("/{brandId}/members")
    public ResponseEntity<BrandResponse> assignMember(
            @PathVariable Long brandId,
            @Valid @RequestBody AssignMemberRequest request) throws IOException {
        return ResponseEntity.ok(brandService.assignMember(brandId, request));
    }

    @PreAuthorize("hasRole('TEAM_MANAGER')")
    @DeleteMapping("/{brandId}/members/{memberId}")
    public ResponseEntity<BrandResponse> unassignMember(
            @PathVariable Long brandId,
            @PathVariable Long memberId) {
        return ResponseEntity.ok(brandService.unassignMember(brandId, memberId));
    }

    @GetMapping("/available-users/{role}")
    public ResponseEntity<List<UserResponse>> getAvailableUsersForRole(@PathVariable String role) {
        Role roleEnum = Role.valueOf(role);
        if (roleEnum == Role.TEAM_MANAGER) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(brandService.getAvailableUsersForRole(roleEnum).stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .role(u.getRole().name())
                        .active(u.isActive())
                        .build())
                .collect(Collectors.toList()));
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
