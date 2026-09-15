package com.googledrive.googleDriveManager.service;

import com.googledrive.googleDriveManager.dto.BrandMemberResponse;
import com.googledrive.googleDriveManager.dto.BrandResponse;
import com.googledrive.googleDriveManager.dto.CreateBrandRequest;
import com.googledrive.googleDriveManager.dto.AssignMemberRequest;
import com.googledrive.googleDriveManager.model.Brand;
import com.googledrive.googleDriveManager.model.BrandMember;
import com.googledrive.googleDriveManager.model.User;
import com.googledrive.googleDriveManager.model.enums.Role;
import com.googledrive.googleDriveManager.repository.BrandMemberRepository;
import com.googledrive.googleDriveManager.repository.BrandRepository;
import com.googledrive.googleDriveManager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandMemberRepository brandMemberRepository;
    private final UserRepository userRepository;
    private final GoogleDriveService googleDriveService;

    public BrandService(BrandRepository brandRepository,
                        BrandMemberRepository brandMemberRepository,
                        UserRepository userRepository,
                        GoogleDriveService googleDriveService) {
        this.brandRepository = brandRepository;
        this.brandMemberRepository = brandMemberRepository;
        this.userRepository = userRepository;
        this.googleDriveService = googleDriveService;
    }

    private static final String PRODUCT_INFO_FOLDER = "⛔ Product info ⛔";
    private static final String VIDEOS_FOLDER = "🎞 Videos";
    private static final String IMAGES_FOLDER = "📸 Images";
    private static final String COPYWRITING_VIDEOS_FOLDER = "🟩 Copywriting videos 🟩";
    private static final String EDITED_VIDEOS_FOLDER = "🟩 Edited videos 🟩";
    private static final String VIDEO_RESULTS_FOLDER = "✨ Video Results ✨";
    private static final String TO_EDIT_VIDEOS_FOLDER = "To Edit Videos";

    @Transactional
    public BrandResponse createBrand(CreateBrandRequest request) throws IOException {
        if (brandRepository.existsByName(request.getName())) {
            throw new RuntimeException("A brand with this name already exists");
        }

        String rootFolderId = googleDriveService.createFolder(request.getName(), null);

        String productInfoId = googleDriveService.createFolder(PRODUCT_INFO_FOLDER, rootFolderId);
        String videosId = googleDriveService.createFolder(VIDEOS_FOLDER, productInfoId);
        String imagesId = googleDriveService.createFolder(IMAGES_FOLDER, productInfoId);
        String copywritingId = googleDriveService.createFolder(COPYWRITING_VIDEOS_FOLDER, rootFolderId);
        String editedVideosId = googleDriveService.createFolder(EDITED_VIDEOS_FOLDER, rootFolderId);
        String videoResultsId = googleDriveService.createFolder(VIDEO_RESULTS_FOLDER, editedVideosId);
        String toEditVideosId = googleDriveService.createFolder(TO_EDIT_VIDEOS_FOLDER, editedVideosId);

        Brand brand = Brand.builder()
                .name(request.getName())
                .rootFolderId(rootFolderId)
                .productInfoFolderId(productInfoId)
                .videosFolderId(videosId)
                .imagesFolderId(imagesId)
                .copywritingVideosFolderId(copywritingId)
                .editedVideosFolderId(editedVideosId)
                .videoResultsFolderId(videoResultsId)
                .toEditVideosFolderId(toEditVideosId)
                .build();

        brand = brandRepository.save(brand);
        return toResponse(brand);
    }

    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BrandResponse getBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
        return toResponse(brand);
    }

    @Transactional
    public BrandResponse assignMember(Long brandId, AssignMemberRequest request) throws IOException {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        Role slotRole = Role.valueOf(request.getRole());

        if (slotRole == Role.TEAM_MANAGER) {
            throw new RuntimeException("TEAM_MANAGER cannot be assigned to a brand slot");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("User is not active");
        }

        if (user.getRole() != slotRole) {
            throw new RuntimeException("User does not have the required role for this slot");
        }

        if (brandMemberRepository.existsByBrandAndUser(brand, user)) {
            throw new RuntimeException("User is already assigned to this brand");
        }

        if (brandMemberRepository.findByBrandAndRole(brand, slotRole).isPresent()) {
            throw new RuntimeException("This slot is already filled for this brand");
        }

        String parentFolderId = getWorkflowFolderForRole(brand, slotRole);
        String userFolderId = googleDriveService.createFolder(user.getUsername(), parentFolderId);

        BrandMember member = BrandMember.builder()
                .brand(brand)
                .user(user)
                .role(slotRole)
                .driveFolderId(userFolderId)
                .build();

        brand.getMembers().add(member);
        brandRepository.save(brand);
        return toResponse(brand);
    }

    @Transactional
    public BrandResponse unassignMember(Long brandId, Long memberId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        BrandMember member = brandMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!member.getBrand().getId().equals(brand.getId())) {
            throw new RuntimeException("Assignment does not belong to this brand");
        }

        brand.getMembers().remove(member);
        brandMemberRepository.delete(member);
        return toResponse(brand);
    }

    public List<User> getAvailableUsersForRole(Role role) {
        return userRepository.findByRoleAndActiveTrue(role);
    }

    public List<Brand> getBrandsForUser(User user) {
        if (user.getRole() == Role.TEAM_MANAGER) {
            return brandRepository.findAll();
        }
        return brandMemberRepository.findByUser(user).stream()
                .map(BrandMember::getBrand)
                .collect(Collectors.toList());
    }

    public Set<String> getAccessibleFolderIds(User user) {
        if (user.getRole() == Role.TEAM_MANAGER) {
            return null; // null = unrestricted
        }
        List<BrandMember> memberships = brandMemberRepository.findByUser(user);
        return memberships.stream()
                .map(BrandMember::getDriveFolderId)
                .collect(Collectors.toSet());
    }

    public boolean canAccessFolder(User user, String folderId) {
        if (user.getRole() == Role.TEAM_MANAGER) {
            return true;
        }
        Set<String> accessible = getAccessibleFolderIds(user);
        return accessible != null && accessible.contains(folderId);
    }

    private String getWorkflowFolderForRole(Brand brand, Role role) {
        return switch (role) {
            case COPYWRITER -> brand.getCopywritingVideosFolderId();
            case VIDEO_GENERATOR -> brand.getVideosFolderId();
            case VIDEO_EDITOR -> brand.getToEditVideosFolderId();
            default -> throw new RuntimeException("No workflow folder for role: " + role);
        };
    }

    public BrandResponse toBrandResponse(Brand brand) {
        return toResponse(brand);
    }

    private BrandResponse toResponse(Brand brand) {
        BrandResponse response = BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .rootFolderId(brand.getRootFolderId())
                .build();

        List<BrandMemberResponse> memberResponses = brand.getMembers().stream()
                .map(m -> BrandMemberResponse.builder()
                        .id(m.getId())
                        .userId(m.getUser().getId())
                        .username(m.getUser().getUsername())
                        .role(m.getRole().name())
                        .driveFolderId(m.getDriveFolderId())
                        .build())
                .collect(Collectors.toList());

        response.setMembers(memberResponses);
        return response;
    }
}
