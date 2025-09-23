package com.example.loanmanagement.Member;

import com.example.loanmanagement.Chama.ChamaEntity;
import com.example.loanmanagement.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    // Existing methods
    Optional<MemberEntity> findByUserId(Long userId);

    boolean existsByUserAndChama(UserEntity user, ChamaEntity chama);

    List<MemberEntity> findByChamaId(Long chamaId);

    List<MemberEntity> findAllByUser(UserEntity user);

    // Additional methods needed for contributions
    List<MemberEntity> findByChama_Id(Long chamaId);

    boolean existsByIdAndChama_Id(Long memberId, Long chamaId);

    @Query("SELECT COUNT(m) FROM MemberEntity m WHERE m.chama.id = :chamaId")
    Integer countMembersByChamaId(@Param("chamaId") Long chamaId);

    @Query("SELECT m FROM MemberEntity m WHERE m.chama.id = :chamaId ORDER BY m.id")
    List<MemberEntity> findByChamaIdOrderById(@Param("chamaId") Long chamaId);
}