package com.example.loanmanagement.Contribution;

import com.example.loanmanagement.Chama.ChamaEntity;
import com.example.loanmanagement.Chama.ChamaRepository;
import com.example.loanmanagement.Member.MemberEntity;
import com.example.loanmanagement.Member.MemberRepository;
import com.example.loanmanagement.User.EmailService;
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

    @Autowired
    private EmailService emailService;

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
            rules = existingRules.get();
            log.info("Updating existing rules for chama {}", dto.getChamaId());
        } else {
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

        if (dto.getPayoutOrder() != null) {
            rules.setPayoutOrder(dto.getPayoutOrder());
        }

        if (dto.getCurrentPayoutMemberId() != null) {
            rules.setCurrentPayoutMemberId(dto.getCurrentPayoutMemberId());
        }

        ChamaRulesEntity saved = chamaRulesRepository.save(rules);
        log.info("Chama rules saved with ID: {}", saved.getId());

        // ✅ Notify all members of this chama
        notifyAllMembers(
                dto.getChamaId(),
                "Chama Rules Updated",
                "The rules for your chama '" + chama.getName() + "' have been created/updated. Please log in to review."
        );

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

        return chamaRulesRepository.findAll().stream()
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

        // ✅ Notify members
        notifyAllMembers(
                chamaId,
                "Chama Rules Deleted ⚠️",
                "The rules for your chama (ID: " + chamaId + ") have been deleted."
        );
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

        // ✅ Notify all members
        notifyAllMembers(
                chamaId,
                "Chama Payout Order Updated",
                "The payout order for your chama '" + rules.getChama().getName() + "' has been updated. Please log in for details."
        );

        return new ChamaRulesDTO(saved);
    }

    @Transactional
    public ChamaRulesDTO updateCurrentPayoutMember(Long chamaId, Long memberId) {
        log.info("Updating current payout member for chama {} to member {}", chamaId, memberId);

        ChamaRulesEntity rules = chamaRulesRepository.findByChamaId(chamaId)
                .orElseThrow(() -> new RuntimeException("Chama rules not found"));

        // Validate member belongs to chama
        if (memberId != null && !memberRepository.existsByIdAndChama_Id(memberId, chamaId)) {
            throw new RuntimeException("Member not found or does not belong to this chama");
        }

        rules.setCurrentPayoutMemberId(memberId);
        ChamaRulesEntity saved = chamaRulesRepository.save(rules);

        // ✅ Notify the payout member only
        if (memberId != null) {
            MemberEntity payoutMember = memberRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));

            String email = payoutMember.getUser().getEmail(); // ✅ fetch from UserEntity inside MemberEntity
            String fullName = payoutMember.getUser().getFullName();

            emailService.sendEmail(
                    email,
                    "🎉 You Are Now the Payout Member",
                    "Hello " + fullName + ",\n\nYou have been set as the current payout member for your chama '" +
                            rules.getChama().getName() + "'.\n\n- ChamaHub Team"
            );
        }

        return new ChamaRulesDTO(saved);
    }

    // ==============================
    // 🔔 Helper Method for Notifications
    // ==============================
    private void notifyAllMembers(Long chamaId, String subject, String message) {
        List<MemberEntity> members = memberRepository.findByChama_Id(chamaId);

        for (MemberEntity member : members) {
            String email = member.getUser().getEmail();     // ✅ get email from UserEntity
            String fullName = member.getUser().getFullName(); // ✅ get full name from UserEntity

            emailService.sendEmail(
                    email,
                    subject,
                    "Hello " + fullName + ",\n\n" + message + "\n\n- ChamaHub Team"
            );
        }
    }
}
