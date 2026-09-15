package com.googledrive.googleDriveManager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "root_folder_id", nullable = false)
    private String rootFolderId;

    @Column(name = "product_info_folder_id", nullable = false)
    private String productInfoFolderId;

    @Column(name = "videos_folder_id", nullable = false)
    private String videosFolderId;

    @Column(name = "images_folder_id", nullable = false)
    private String imagesFolderId;

    @Column(name = "copywriting_videos_folder_id", nullable = false)
    private String copywritingVideosFolderId;

    @Column(name = "edited_videos_folder_id", nullable = false)
    private String editedVideosFolderId;

    @Column(name = "video_results_folder_id", nullable = false)
    private String videoResultsFolderId;

    @Column(name = "to_edit_videos_folder_id", nullable = false)
    private String toEditVideosFolderId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "brand",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<BrandMember> members = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
