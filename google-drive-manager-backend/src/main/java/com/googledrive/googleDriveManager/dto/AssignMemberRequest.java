package com.googledrive.googleDriveManager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignMemberRequest {
    @NotNull
    private Long userId;
    @NotBlank
    private String role;
}
