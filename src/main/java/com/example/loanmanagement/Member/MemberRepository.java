package com.example.loanmanagement.Member;

import com.example.loanmanagement.Chama.ChamaEntity;
import com.example.loanmanagement.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    // For single chama lookups if needed
    Optional<MemberEntity> findByUserId(Long userId);

    // Check if a user already belongs to a given chama
    boolean existsByUserAndChama(UserEntity user, ChamaEntity chama);

    // Get all members of a chama
    List<MemberEntity> findByChamaId(Long chamaId);

    // ✅ Multi-chama support: get all memberships of a user
    List<MemberEntity> findAllByUser(UserEntity user);
}
