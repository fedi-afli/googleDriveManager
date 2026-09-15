package com.googledrive.googleDriveManager.model;

import com.googledrive.googleDriveManager.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "brand_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_brand_role_slot", columnNames = {"brand_id", "role"}),
                @UniqueConstraint(name = "uk_brand_user", columnNames = {"brand_id", "user_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "drive_folder_id", nullable = false)
    private String driveFolderId;
}
