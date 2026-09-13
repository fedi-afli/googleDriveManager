package com.googledrive.googleDriveManager.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileItem {
    private String id;
    private String name;
    private String mimeType;
    private boolean folder;
    private List<String> parents;
}
