package com.googledrive.googleDriveManager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveFileRequest {
    @NotBlank
    private String targetFolderId;
}
