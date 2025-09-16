package com.example.loanmanagement.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUserId(String userId);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    List<UserEntity> findByStatus(UserStatus status); // Retrieves users by their approval status
}
