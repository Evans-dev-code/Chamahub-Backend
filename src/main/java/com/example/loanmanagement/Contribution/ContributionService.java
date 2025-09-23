package com.example.loanmanagement.Contribution;

import com.example.loanmanagement.Member.MemberEntity;
import com.example.loanmanagement.Member.MemberRepository;
import com.example.loanmanagement.Chama.ChamaEntity;
import com.example.loanmanagement.Chama.ChamaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContributionService {

    @Autowired
    private ContributionRepository contributionRepository;

    @Autowired
    private ChamaRulesRepository chamaRulesRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChamaRepository chamaRepository;

    @Transactional
    public ContributionDTO addContribution(ContributionDTO dto) {
        log.info("Adding contribution for member {} in chama {}", dto.getMemberId(), dto.getChamaId());

        // Validate member and chama exist
        MemberEntity member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        ChamaEntity chama = chamaRepository.findById(dto.getChamaId())
                .orElseThrow(() -> new RuntimeException("Chama not found"));

        // Validate member belongs to chama
        if (!member.getChama().getId().equals(dto.getChamaId())) {
            throw new RuntimeException("Member does not belong to the specified chama");
        }

        // Get chama rules
        Optional<ChamaRulesEntity> rulesOpt = chamaRulesRepository.findByChamaId(dto.getChamaId());
        if (rulesOpt.isEmpty()) {
            throw new RuntimeException("Chama rules not configured. Please set contribution rules first.");
        }

        ChamaRulesEntity rules = rulesOpt.get();

        // Check if contribution already exists for this member, chama, and cycle
        Optional<ContributionEntity> existingContribution = contributionRepository
                .findByMemberIdAndChamaIdAndCycle(dto.getMemberId(), dto.getChamaId(), dto.getCycle());

        if (existingContribution.isPresent()) {
            throw new RuntimeException("Contribution already exists for this member in the specified cycle");
        }

        // Determine if contribution is on time or late
        ContributionEntity.ContributionStatus status = determineContributionStatus(dto.getDatePaid(), dto.getCycle(), rules);

        // Calculate penalty if late
        BigDecimal penaltyAmount = BigDecimal.ZERO;
        if (status == ContributionEntity.ContributionStatus.LATE) {
            penaltyAmount = rules.getPenaltyForLate();
        }

        // Create contribution entity
        ContributionEntity contribution = new ContributionEntity();
        contribution.setAmount(dto.getAmount());
        contribution.setDatePaid(dto.getDatePaid());
        contribution.setCycle(dto.getCycle());
        contribution.setStatus(status);
        contribution.setMember(member);
        contribution.setChama(chama);
        contribution.setPenaltyAmount(penaltyAmount);
        contribution.setNotes(dto.getNotes());

        ContributionEntity saved = contributionRepository.save(contribution);
        log.info("Contribution saved with ID: {}", saved.getId());

        return new ContributionDTO(saved);
    }

    public List<ContributionDTO> getContributionsByChama(Long chamaId, String cycle) {
        log.info("Fetching contributions for chama {} and cycle {}", chamaId, cycle);

        List<ContributionEntity> contributions;
        if (cycle != null && !cycle.trim().isEmpty()) {
            contributions = contributionRepository.findByChamaIdAndCycle(chamaId, cycle);
        } else {
            contributions = contributionRepository.findByChamaId(chamaId);
        }

        return contributions.stream()
                .map(ContributionDTO::new)
                .collect(Collectors.toList());
    }

    public List<ContributionDTO> getContributionsByMember(Long memberId) {
        log.info("Fetching contributions for member {}", memberId);

        List<ContributionEntity> contributions = contributionRepository.findByMemberId(memberId);
        return contributions.stream()
                .map(ContributionDTO::new)
                .collect(Collectors.toList());
    }

    public ContributionOwedDTO calculateOwedAmount(Long memberId, Long chamaId) {
        log.info("Calculating owed amount for member {} in chama {}", memberId, chamaId);

        // Validate member and chama
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        ChamaEntity chama = chamaRepository.findById(chamaId)
                .orElseThrow(() -> new RuntimeException("Chama not found"));

        // Get chama rules
        ChamaRulesEntity rules = chamaRulesRepository.findByChamaId(chamaId)
                .orElseThrow(() -> new RuntimeException("Chama rules not found"));

        // Get current cycle
        String currentCycle = getCurrentCycle(rules.getCycleType());

        // Check if member has contributed for current cycle
        Optional<ContributionEntity> currentContribution = contributionRepository
                .findByMemberIdAndChamaIdAndCycle(memberId, chamaId, currentCycle);

        ContributionOwedDTO result = new ContributionOwedDTO();
        result.setMemberId(memberId);
        result.setChamaId(chamaId);
        result.setCurrentCycle(currentCycle);
        result.setExpectedAmount(rules.getMonthlyContributionAmount());

        if (currentContribution.isPresent()) {
            result.setAmountOwed(BigDecimal.ZERO);
            result.setStatus("PAID");
            result.setLastPaymentDate(currentContribution.get().getDatePaid());
        } else {
            result.setAmountOwed(rules.getMonthlyContributionAmount());
            result.setStatus("PENDING");

            // Calculate if overdue
            LocalDate dueDate = calculateDueDate(currentCycle, rules);
            if (LocalDate.now().isAfter(dueDate.plusDays(rules.getGracePeriodDays()))) {
                result.setStatus("OVERDUE");
                result.setAmountOwed(result.getAmountOwed().add(rules.getPenaltyForLate()));
                result.setPenaltyAmount(rules.getPenaltyForLate());
            }

            result.setDueDate(dueDate);
        }

        return result;
    }

    public BigDecimal calculateTotalContributions(Long chamaId, String cycle) {
        if (cycle != null && !cycle.trim().isEmpty()) {
            return contributionRepository.getTotalContributionsByChamaAndCycle(chamaId, cycle);
        } else {
            return contributionRepository.findByChamaId(chamaId).stream()
                    .map(ContributionEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    // Merry-go-round logic
    public MemberPayoutDTO calculateNextPayout(Long chamaId) {
        log.info("Calculating next payout for chama {}", chamaId);

        ChamaRulesEntity rules = chamaRulesRepository.findByChamaId(chamaId)
                .orElseThrow(() -> new RuntimeException("Chama rules not found"));

        String currentCycle = getCurrentCycle(rules.getCycleType());
        BigDecimal totalCollected = calculateTotalContributions(chamaId, currentCycle);

        // Get current payout member or determine next
        Long nextPayoutMemberId = rules.getCurrentPayoutMemberId();
        if (nextPayoutMemberId == null) {
            // Initialize payout rotation
            List<MemberEntity> members = memberRepository.findByChama_Id(chamaId);
            if (!members.isEmpty()) {
                nextPayoutMemberId = members.get(0).getId();
            }
        }

        MemberPayoutDTO result = new MemberPayoutDTO();
        result.setChamaId(chamaId);
        result.setCycle(currentCycle);
        result.setNextPayoutMemberId(nextPayoutMemberId);
        result.setPayoutAmount(totalCollected);

        return result;
    }

    // Placeholder for future profit sharing
    public void distributeDividends(Long chamaId) {
        log.info("Distributing dividends for chama {} - Feature not yet implemented", chamaId);
        // TODO: Implement dividend distribution logic
        throw new RuntimeException("Dividend distribution feature coming soon");
    }

    // Helper methods
    private ContributionEntity.ContributionStatus determineContributionStatus(LocalDate datePaid, String cycle, ChamaRulesEntity rules) {
        LocalDate dueDate = calculateDueDate(cycle, rules);
        LocalDate gracePeriodEnd = dueDate.plusDays(rules.getGracePeriodDays());

        if (datePaid.isAfter(gracePeriodEnd)) {
            return ContributionEntity.ContributionStatus.LATE;
        } else {
            return ContributionEntity.ContributionStatus.ON_TIME;
        }
    }

    private LocalDate calculateDueDate(String cycle, ChamaRulesEntity rules) {
        if (rules.getCycleType() == ChamaRulesEntity.CycleType.MONTHLY) {
            // Parse cycle like "January 2025"
            String[] parts = cycle.split(" ");
            if (parts.length == 2) {
                int year = Integer.parseInt(parts[1]);
                int month = getMonthNumber(parts[0]);
                return LocalDate.of(year, month, Math.min(rules.getDayOfCycle(), 28));
            }
        }
        // Default fallback
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusDays(rules.getDayOfCycle() - 1);
    }

    private String getCurrentCycle(ChamaRulesEntity.CycleType cycleType) {
        LocalDate now = LocalDate.now();
        if (cycleType == ChamaRulesEntity.CycleType.MONTHLY) {
            return now.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        } else {
            // Weekly implementation
            int weekOfYear = now.getDayOfYear() / 7 + 1;
            return "Week " + weekOfYear + " " + now.getYear();
        }
    }

    private int getMonthNumber(String monthName) {
        return switch (monthName.toLowerCase()) {
            case "january" -> 1;
            case "february" -> 2;
            case "march" -> 3;
            case "april" -> 4;
            case "may" -> 5;
            case "june" -> 6;
            case "july" -> 7;
            case "august" -> 8;
            case "september" -> 9;
            case "october" -> 10;
            case "november" -> 11;
            case "december" -> 12;
            default -> 1;
        };
    }

    public List<String> getAvailableCycles(Long chamaId) {
        return contributionRepository.getDistinctCyclesByChamaId(chamaId);
    }
}