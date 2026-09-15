package com.googledrive.googleDriveManager.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandResponse {
    private Long id;
    private String name;
    private String rootFolderId;
    private List<BrandMemberResponse> members;
}
