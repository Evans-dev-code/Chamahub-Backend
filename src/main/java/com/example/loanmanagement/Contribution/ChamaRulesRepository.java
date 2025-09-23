package com.example.loanmanagement.Contribution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChamaRulesRepository extends JpaRepository<ChamaRulesEntity, Long> {

    // Find rules by chama ID
    Optional<ChamaRulesEntity> findByChamaId(Long chamaId);

    // Check if rules exist for a chama
    boolean existsByChamaId(Long chamaId);

    // Delete rules by chama ID
    void deleteByChamaId(Long chamaId);

    // Find rules with specific cycle type
    @Query("SELECT cr FROM ChamaRulesEntity cr WHERE cr.chama.id = :chamaId AND cr.cycleType = :cycleType")
    Optional<ChamaRulesEntity> findByChamaIdAndCycleType(@Param("chamaId") Long chamaId,
                                                         @Param("cycleType") ChamaRulesEntity.CycleType cycleType);
}