package com.googledrive.googleDriveManager.config;

import com.googledrive.googleDriveManager.model.enums.Role;
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
            String adminUsername = "moorahal";
            if (!userRepository.existsByUsername(adminUsername)) {
                User admin = User.builder()
                        .username(adminUsername)
                        .password(passwordEncoder.encode("tawtaw"))
                        .role(Role.TEAM_MANAGER)
                        .active(true)
                        .build();
                userRepository.save(admin);
            }
        };
    }
}
