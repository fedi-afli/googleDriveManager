package com.googledrive.googleDriveManager.repository;

import com.googledrive.googleDriveManager.model.GoogleToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoogleTokenRepository extends JpaRepository<GoogleToken, Long> {
    Optional<GoogleToken> findFirstByOrderByIdAsc();
    boolean existsByAccountEmail(String email);
    void deleteByAccountEmail(String email);
}
