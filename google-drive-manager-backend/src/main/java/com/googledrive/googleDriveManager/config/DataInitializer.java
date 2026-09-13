package com.googledrive.googleDriveManager.config;

import com.googledrive.googleDriveManager.model.Role;
import com.googledrive.googleDriveManager.model.User;
import com.googledrive.googleDriveManager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminUsername = "Mootaz Rahal";
            if (!userRepository.existsByUsername(adminUsername)) {
                User admin = User.builder()
                        .username(adminUsername)
                        .password(passwordEncoder.encode("tawta5rafih"))
                        .role(Role.TEAM_MANAGER)
                        .build();
                userRepository.save(admin);
            }
        };
    }
}
