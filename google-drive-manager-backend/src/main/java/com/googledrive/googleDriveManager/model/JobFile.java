
        package com.googledrive.googleDriveManager.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


        @Getter
@Setter
@Entity
@Table(
        name = "job_files",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"job_id", "drive_file_id"})
        }
)
public class JobFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "drive_file_id", nullable = false)
    private String driveFileId;
}

