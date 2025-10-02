package com.example.loanmanagement.Member;

import com.example.loanmanagement.Chama.ChamaEntity;
import com.example.loanmanagement.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    // Fetch a member by user ID and chama ID (important for contributions)
    Optional<MemberEntity> findByUser_IdAndChama_Id(Long userId, Long chamaId);

    // Check if a user is already a member of a specific chama
    boolean existsByUserAndChama(UserEntity user, ChamaEntity chama);

    // Check if a member exists within a chama
    boolean existsByIdAndChama_Id(Long memberId, Long chamaId);

    // Fetch all members of a specific chama
    List<MemberEntity> findByChama_Id(Long chamaId);

    // Fetch all members of a specific chama, ordered by ID
    @Query("SELECT m FROM MemberEntity m WHERE m.chama.id = :chamaId ORDER BY m.id")
    List<MemberEntity> findByChamaIdOrderById(@Param("chamaId") Long chamaId);

    // Count total members in a chama
    @Query("SELECT COUNT(m) FROM MemberEntity m WHERE m.chama.id = :chamaId")
    Integer countMembersByChamaId(@Param("chamaId") Long chamaId);

    // Fetch all memberships for a user
    List<MemberEntity> findAllByUser(UserEntity user);

    // Fetch a member by user ID
    Optional<MemberEntity> findByUser_Id(Long userId);
}
