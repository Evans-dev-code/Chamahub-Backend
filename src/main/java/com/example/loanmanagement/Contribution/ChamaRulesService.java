package com.example.loanmanagement.Contribution;

import com.example.loanmanagement.Chama.ChamaEntity;
import com.example.loanmanagement.Chama.ChamaRepository;
import com.example.loanmanagement.Member.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChamaRulesService {

    @Autowired
    private ChamaRulesRepository chamaRulesRepository;

    @Autowired
    private ChamaRepository chamaRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Transactional
    public ChamaRulesDTO createOrUpdateChamaRules(ChamaRulesDTO dto) {
        log.info("Creating/updating chama rules for chama {}", dto.getChamaId());

        // Validate chama exists
        ChamaEntity chama = chamaRepository.findById(dto.getChamaId())
                .orElseThrow(() -> new RuntimeException("Chama not found"));

        // Check if rules already exist
        Optional<ChamaRulesEntity> existingRules = chamaRulesRepository.findByChamaId(dto.getChamaId());

        ChamaRulesEntity rules;
        if (existingRules.isPresent()) {
            // Update existing rules
            rules = existingRules.get();
            log.info("Updating existing rules for chama {}", dto.getChamaId());
        } else {
            // Create new rules
            rules = new ChamaRulesEntity();
            rules.setChama(chama);
            log.info("Creating new rules for chama {}", dto.getChamaId());
        }

        // Set/update rule values
        rules.setMonthlyContributionAmount(dto.getMonthlyContributionAmount());
        rules.setPenaltyForLate(dto.getPenaltyForLate());
        rules.setCycleType(dto.getCycleType());
        rules.setDayOfCycle(dto.getDayOfCycle());
        rules.setGracePeriodDays(dto.getGracePeriodDays());

        // Handle payout order if provided
        if (dto.getPayoutOrder() != null) {
            rules.setPayoutOrder(dto.getPayoutOrder());
        }

        if (dto.getCurrentPayoutMemberId() != null) {
            rules.setCurrentPayoutMemberId(dto.getCurrentPayoutMemberId());
        }

        ChamaRulesEntity saved = chamaRulesRepository.save(rules);
        log.info("Chama rules saved with ID: {}", saved.getId());

        return new ChamaRulesDTO(saved);
    }

    public ChamaRulesDTO getChamaRules(Long chamaId) {
        log.info("Fetching chama rules for chama {}", chamaId);

        ChamaRulesEntity rules = chamaRulesRepository.findByChamaId(chamaId)
                .orElseThrow(() -> new RuntimeException("Chama rules not found for chama ID: " + chamaId));

        return new ChamaRulesDTO(rules);
    }

    public List<ChamaRulesDTO> getAllChamaRules() {
        log.info("Fetching all chama rules");

        List<ChamaRulesEntity> allRules = chamaRulesRepository.findAll();
        return allRules.stream()
                .map(ChamaRulesDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteChamaRules(Long chamaId) {
        log.info("Deleting chama rules for chama {}", chamaId);

        if (!chamaRulesRepository.existsByChamaId(chamaId)) {
            throw new RuntimeException("No rules found for chama ID: " + chamaId);
        }

        chamaRulesRepository.deleteByChamaId(chamaId);
        log.info("Chama rules deleted for chama {}", chamaId);
    }

    public boolean chamaRulesExist(Long chamaId) {
        return chamaRulesRepository.existsByChamaId(chamaId);
    }

    @Transactional
    public ChamaRulesDTO updatePayoutOrder(Long chamaId, String payoutOrder) {
        log.info("Updating payout order for chama {}", chamaId);

        ChamaRulesEntity rules = chamaRulesRepository.findByChamaId(chamaId)
                .orElseThrow(() -> new RuntimeException("Chama rules not found"));

        rules.setPayoutOrder(payoutOrder);
        ChamaRulesEntity saved = chamaRulesRepository.save(rules);

        return new ChamaRulesDTO(saved);
    }

    @Transactional
    public ChamaRulesDTO updateCurrentPayoutMember(Long chamaId, Long memberId) {
        log.info("Updating current payout member for chama {} to member {}", chamaId, memberId);

        ChamaRulesEntity rules = chamaRulesRepository.findByChamaId(chamaId)
                .orElseThrow(() -> new RuntimeException("Chama rules not found"));

        // Validate member exists and belongs to chama
        if (memberId != null) {
            boolean memberExists = memberRepository.existsByIdAndChama_Id(memberId, chamaId);
            if (!memberExists) {
                throw new RuntimeException("Member not found or does not belong to this chama");
            }
        }

        rules.setCurrentPayoutMemberId(memberId);
        ChamaRulesEntity saved = chamaRulesRepository.save(rules);

        return new ChamaRulesDTO(saved);
    }
}