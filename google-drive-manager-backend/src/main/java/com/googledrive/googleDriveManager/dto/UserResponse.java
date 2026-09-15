package com.googledrive.googleDriveManager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String role;
    private boolean active;
    private String password;
}
