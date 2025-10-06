package com.example.loanmanagement.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminInvitationRepository extends JpaRepository<AdminInvitationEntity, Long> {
    Optional<AdminInvitationEntity> findByToken(String token);
    boolean existsByEmailAndAcceptedFalse(String email);
}
