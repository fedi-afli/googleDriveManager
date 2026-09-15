package com.googledrive.googleDriveManager.model;

import com.googledrive.googleDriveManager.model.enums.DeleteRequestState;
import com.googledrive.googleDriveManager.model.enums.DriveItemType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "delete_requests")
public class DeleteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User who requested the deletion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // Manager who approved/rejected the request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    // Google Drive file/folder ID
    @Column(name = "drive_item_id", nullable = false)
    private String driveItemId;

    // FILE or FOLDER
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private DriveItemType itemType;

    @Column
    private String itemName;

    @Column
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeleteRequestState state = DeleteRequestState.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;
}