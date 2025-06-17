package com.example.loanmanagement.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // Find by username or email (helpful for login flexibility)
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUserId(String userId);

    // Checks to prevent duplicate usernames or emails during signup
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
