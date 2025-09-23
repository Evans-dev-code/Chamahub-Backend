package com.example.loanmanagement.Contribution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContributionRepository extends JpaRepository<ContributionEntity, Long> {

    // Find contributions by chama and cycle
    List<ContributionEntity> findByChamaIdAndCycle(Long chamaId, String cycle);

    // Find contributions by member
    List<ContributionEntity> findByMemberId(Long memberId);

    // Find contributions by member and chama
    List<ContributionEntity> findByMemberIdAndChamaId(Long memberId, Long chamaId);

    // Find all contributions for a chama
    List<ContributionEntity> findByChamaId(Long chamaId);

    // Find contributions by chama and status
    List<ContributionEntity> findByChamaIdAndStatus(Long chamaId, ContributionEntity.ContributionStatus status);

    // Find member's contributions for specific chama and cycle
    Optional<ContributionEntity> findByMemberIdAndChamaIdAndCycle(Long memberId, Long chamaId, String cycle);

    // Get total contributions by member in a chama
    @Query("SELECT SUM(c.amount) FROM ContributionEntity c WHERE c.member.id = :memberId AND c.chama.id = :chamaId")
    BigDecimal getTotalContributionsByMemberAndChama(@Param("memberId") Long memberId, @Param("chamaId") Long chamaId);

    // Get total contributions for a chama in a specific cycle
    @Query("SELECT SUM(c.amount) FROM ContributionEntity c WHERE c.chama.id = :chamaId AND c.cycle = :cycle")
    BigDecimal getTotalContributionsByChamaAndCycle(@Param("chamaId") Long chamaId, @Param("cycle") String cycle);

    // Find contributions within date range
    @Query("SELECT c FROM ContributionEntity c WHERE c.chama.id = :chamaId AND c.datePaid BETWEEN :startDate AND :endDate")
    List<ContributionEntity> findByChamaIdAndDatePaidBetween(@Param("chamaId") Long chamaId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    // Get pending contributions for a member
    @Query("SELECT c FROM ContributionEntity c WHERE c.member.id = :memberId AND c.status = 'PENDING'")
    List<ContributionEntity> findPendingContributionsByMember(@Param("memberId") Long memberId);

    // Get late contributions for a chama
    List<ContributionEntity> findByChamaIdAndStatusOrderByDatePaidDesc(Long chamaId, ContributionEntity.ContributionStatus status);

    // Count contributions by member and chama
    @Query("SELECT COUNT(c) FROM ContributionEntity c WHERE c.member.id = :memberId AND c.chama.id = :chamaId")
    Long countContributionsByMemberAndChama(@Param("memberId") Long memberId, @Param("chamaId") Long chamaId);

    // Find latest contribution by member in chama
    @Query("SELECT c FROM ContributionEntity c WHERE c.member.id = :memberId AND c.chama.id = :chamaId ORDER BY c.datePaid DESC LIMIT 1")
    Optional<ContributionEntity> findLatestContributionByMemberAndChama(@Param("memberId") Long memberId, @Param("chamaId") Long chamaId);

    // Get distinct cycles for a chama (for reporting)
    @Query("SELECT DISTINCT c.cycle FROM ContributionEntity c WHERE c.chama.id = :chamaId ORDER BY c.cycle")
    List<String> getDistinctCyclesByChamaId(@Param("chamaId") Long chamaId);
}