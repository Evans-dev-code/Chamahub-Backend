package com.example.loanmanagement.Chama;

import com.example.loanmanagement.Chama.ChamaEntity;
import com.example.loanmanagement.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChamaRepository extends JpaRepository<ChamaEntity, Long> {
    Optional<ChamaEntity> findByJoinCode(String joinCode);
    boolean existsByName(String name);
}
