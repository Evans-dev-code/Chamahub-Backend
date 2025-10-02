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

    // ===== By Chama =====
    List<ContributionEntity> findByChamaId(Long chamaId);

    List<ContributionEntity> findByChamaIdAndCycle(Long chamaId, String cycle);

    List<ContributionEntity> findByChamaIdAndStatus(Long chamaId, ContributionEntity.ContributionStatus status);

    List<ContributionEntity> findByChamaIdAndStatusOrderByDatePaidDesc(Long chamaId, ContributionEntity.ContributionStatus status);

    // ===== By Member =====
    List<ContributionEntity> findByMemberId(Long memberId);

    List<ContributionEntity> findByMemberIdAndChamaId(Long memberId, Long chamaId);

    List<ContributionEntity> findByMemberIdAndChamaIdAndCycle(Long memberId, Long chamaId, String cycle);

    List<ContributionEntity> findByMemberIdAndStatus(Long memberId, ContributionEntity.ContributionStatus status);

    // Latest contribution for a member in a chama
    Optional<ContributionEntity> findFirstByMemberIdAndChamaIdOrderByDatePaidDesc(Long memberId, Long chamaId);

    // Latest contribution for a member in a chama and cycle (needed for owed calculations)
    Optional<ContributionEntity> findFirstByMemberIdAndChamaIdAndCycleOrderByDatePaidDesc(
            Long memberId, Long chamaId, String cycle
    );

    Long countByMemberIdAndChamaId(Long memberId, Long chamaId);

    // ===== Date Range Queries =====
    @Query("SELECT c FROM ContributionEntity c WHERE c.chama.id = :chamaId AND c.datePaid BETWEEN :startDate AND :endDate")
    List<ContributionEntity> findByChamaIdAndDatePaidBetween(
            @Param("chamaId") Long chamaId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // ===== Total Contributions =====
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM ContributionEntity c WHERE c.member.id = :memberId AND c.chama.id = :chamaId")
    BigDecimal getTotalContributionsByMemberAndChama(
            @Param("memberId") Long memberId,
            @Param("chamaId") Long chamaId
    );

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM ContributionEntity c WHERE c.chama.id = :chamaId AND (:cycle IS NULL OR c.cycle = :cycle)")
    BigDecimal getTotalContributionsByChamaAndCycle(
            @Param("chamaId") Long chamaId,
            @Param("cycle") String cycle
    );

    // ===== Distinct Cycles =====
    @Query("SELECT DISTINCT c.cycle FROM ContributionEntity c WHERE c.chama.id = :chamaId ORDER BY c.cycle")
    List<String> getDistinctCyclesByChamaId(@Param("chamaId") Long chamaId);
}
