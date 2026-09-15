package com.googledrive.googleDriveManager.repository;

import com.googledrive.googleDriveManager.model.Brand;
import com.googledrive.googleDriveManager.model.BrandMember;
import com.googledrive.googleDriveManager.model.User;
import com.googledrive.googleDriveManager.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandMemberRepository extends JpaRepository<BrandMember, Long> {
    List<BrandMember> findByBrand(Brand brand);
    List<BrandMember> findByUser(User user);
    Optional<BrandMember> findByBrandAndRole(Brand brand, Role role);
    Optional<BrandMember> findByBrandAndUser(Brand brand, User user);
    boolean existsByBrandAndUser(Brand brand, User user);
}
