package com.googledrive.googleDriveManager.repository;

import com.googledrive.googleDriveManager.model.User;
import com.googledrive.googleDriveManager.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByRoleAndActiveTrue(Role role);
}
