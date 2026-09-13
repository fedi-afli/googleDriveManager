package com.googledrive.googleDriveManager.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "google_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountEmail;

    @Column(nullable = false, length = 4096)
    private String refreshToken;

    @Column(length = 4096)
    private String accessToken;

    private Long expiresAtMs;
}
