package com.googledrive.googleDriveManager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandMemberResponse {
    private Long id;
    private Long userId;
    private String username;
    private String role;
    private String driveFolderId;
}
