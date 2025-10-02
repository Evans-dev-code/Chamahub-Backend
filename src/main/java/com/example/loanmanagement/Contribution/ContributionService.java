package com.example.loanmanagement.Contribution;

import com.example.loanmanagement.Member.MemberEntity;
import com.example.loanmanagement.Member.MemberRepository;
import com.example.loanmanagement.Chama.ChamaEntity;
import com.example.loanmanagement.Chama.ChamaRepository;
import com.example.loanmanagement.User.EmailService;
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

    @Autowired
    private EmailService emailService;

    // ===== Add Contribution =====
    @Transactional
    public ContributionDTO addContribution(ContributionDTO dto, Long userId) {
        log.info("Adding contribution for user {} in chama {}", userId, dto.getChamaId());

        MemberEntity member = memberRepository
                .findByUser_IdAndChama_Id(userId, dto.getChamaId())
                .orElseThrow(() -> new RuntimeException("Member not found for this user in the specified chama"));

        ChamaEntity chama = chamaRepository.findById(dto.getChamaId())
                .orElseThrow(() -> new RuntimeException("Chama not found"));

        ChamaRulesEntity rules = chamaRulesRepository.findByChamaId(dto.getChamaId())
                .orElseThrow(() -> new RuntimeException("Chama rules not configured. Please set contribution rules first."));

        // Use Optional properly to check existing contribution
        Optional<ContributionEntity> existingContribution = contributionRepository
                .findFirstByMemberIdAndChamaIdAndCycleOrderByDatePaidDesc(member.getId(), dto.getChamaId(), dto.getCycle());

        if (existingContribution.isPresent()) {
            throw new RuntimeException("Contribution already exists for this member in the specified cycle");
        }

        ContributionEntity.ContributionStatus status = determineContributionStatus(dto.getDatePaid(), dto.getCycle(), rules);
        BigDecimal penaltyAmount = status == ContributionEntity.ContributionStatus.LATE
                ? rules.getPenaltyForLate()
                : BigDecimal.ZERO;

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

        // ===== Send Email Notification =====
        try {
            String subject = "Contribution Recorded - " + chama.getName();
            String message = "Dear " + member.getUser().getFullName() + ",\n\n" +
                    "We have successfully recorded your contribution of " + dto.getAmount() +
                    " for cycle: " + dto.getCycle() + " in chama: " + chama.getName() + ".\n\n" +
                    "Status: " + status +
                    (penaltyAmount.compareTo(BigDecimal.ZERO) > 0 ? ("\nPenalty Applied: " + penaltyAmount) : "") +
                    "\n\nThank you for your contribution.\n\n" +
                    "Chama Management System";

            emailService.sendEmail(member.getUser().getEmail(), subject, message);
            log.info("Contribution email sent to {}", member.getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send contribution email to {}: {}", member.getUser().getEmail(), e.getMessage());
        }

        return new ContributionDTO(saved);
    }

    // ===== Get Contributions by Chama =====
    public List<ContributionDTO> getContributionsByChama(Long chamaId, String cycle) {
        List<ContributionEntity> contributions = (cycle != null && !cycle.trim().isEmpty())
                ? contributionRepository.findByChamaIdAndCycle(chamaId, cycle)
                : contributionRepository.findByChamaId(chamaId);
        return contributions.stream().map(ContributionDTO::new).collect(Collectors.toList());
    }

    // ===== Get Contributions by Member =====
    public List<ContributionDTO> getContributionsByMember(Long userId, Long chamaId, String cycle) {
        MemberEntity member = memberRepository
                .findByUser_IdAndChama_Id(userId, chamaId)
                .orElseThrow(() -> new RuntimeException("Member not found for this user in the specified chama"));

        List<ContributionEntity> contributions;

        if (cycle != null && !cycle.trim().isEmpty()) {
            contributions = contributionRepository.findByMemberIdAndChamaIdAndCycle(member.getId(), chamaId, cycle);
        } else {
            contributions = contributionRepository.findByMemberIdAndChamaId(member.getId(), chamaId);
        }

        return contributions.stream()
                .map(ContributionDTO::new)
                .collect(Collectors.toList());
    }

    // ===== Calculate Owed Amount =====
    public ContributionOwedDTO calculateOwedAmount(Long userId, Long chamaId) {
        MemberEntity member = memberRepository
                .findByUser_IdAndChama_Id(userId, chamaId)
                .orElseThrow(() -> new RuntimeException("Member not found for this user in the specified chama"));

        ChamaRulesEntity rules = chamaRulesRepository.findByChamaId(chamaId)
                .orElseThrow(() -> new RuntimeException("Chama rules not found"));

        String currentCycle = getCurrentCycle(rules.getCycleType());

        Optional<ContributionEntity> currentContribution = contributionRepository
                .findFirstByMemberIdAndChamaIdAndCycleOrderByDatePaidDesc(member.getId(), chamaId, currentCycle);

        ContributionOwedDTO result = new ContributionOwedDTO();
        result.setMemberId(member.getUser().getId());
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

            LocalDate dueDate = calculateDueDate(currentCycle, rules);
            if (LocalDate.now().isAfter(dueDate.plusDays(rules.getGracePeriodDays()))) {
                result.setStatus("OVERDUE");
                result.setAmountOwed(result.getAmountOwed().add(rules.getPenaltyForLate()));
                result.setPenaltyAmount(rules.getPenaltyForLate());

                // Send overdue email
                try {
                    String subject = "Contribution Overdue - " + chamaRepository.findById(chamaId).get().getName();
                    String message = "Dear " + member.getUser().getFullName() + ",\n\n" +
                            "Your contribution for cycle: " + currentCycle + " in chama: " +
                            chamaRepository.findById(chamaId).get().getName() +
                            " is OVERDUE.\n\n" +
                            "Amount Due: " + result.getAmountOwed() +
                            "\nPenalty Applied: " + rules.getPenaltyForLate() +
                            "\nPlease make your payment as soon as possible.\n\n" +
                            "Chama Management System";

                    emailService.sendEmail(member.getUser().getEmail(), subject, message);
                    log.info("Overdue email sent to {}", member.getUser().getEmail());
                } catch (Exception e) {
                    log.error("Failed to send overdue email: {}", e.getMessage());
                }
            }
            result.setDueDate(dueDate);
        }

        return result;
    }

    // ===== Total Contributions =====
    public BigDecimal calculateTotalContributions(Long chamaId, String cycle) {
        if (cycle != null && !cycle.trim().isEmpty()) {
            return contributionRepository.getTotalContributionsByChamaAndCycle(chamaId, cycle);
        } else {
            return contributionRepository.findByChamaId(chamaId)
                    .stream()
                    .map(ContributionEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    // ===== Next Payout =====
    public MemberPayoutDTO calculateNextPayout(Long chamaId) {
        ChamaRulesEntity rules = chamaRulesRepository.findByChamaId(chamaId)
                .orElseThrow(() -> new RuntimeException("Chama rules not found"));

        String currentCycle = getCurrentCycle(rules.getCycleType());
        BigDecimal totalCollected = calculateTotalContributions(chamaId, currentCycle);

        Long nextPayoutMemberId = rules.getCurrentPayoutMemberId();
        if (nextPayoutMemberId == null) {
            List<MemberEntity> members = memberRepository.findByChama_Id(chamaId);
            if (!members.isEmpty()) {
                nextPayoutMemberId = members.get(0).getUser().getId();
            }
        }

        MemberPayoutDTO result = new MemberPayoutDTO();
        result.setChamaId(chamaId);
        result.setCycle(currentCycle);
        result.setNextPayoutMemberId(nextPayoutMemberId);
        result.setPayoutAmount(totalCollected);
        return result;
    }

    // ===== Dividend Distribution =====
    public void distributeDividends(Long chamaId) {
        throw new RuntimeException("Dividend distribution feature coming soon");
    }

    // ===== Helper Methods =====
    private ContributionEntity.ContributionStatus determineContributionStatus(LocalDate datePaid, String cycle, ChamaRulesEntity rules) {
        LocalDate dueDate = calculateDueDate(cycle, rules);
        LocalDate gracePeriodEnd = dueDate.plusDays(rules.getGracePeriodDays());
        return datePaid.isAfter(gracePeriodEnd)
                ? ContributionEntity.ContributionStatus.LATE
                : ContributionEntity.ContributionStatus.ON_TIME;
    }

    private LocalDate calculateDueDate(String cycle, ChamaRulesEntity rules) {
        if (rules.getCycleType() == ChamaRulesEntity.CycleType.MONTHLY) {
            String[] parts = cycle.split(" ");
            if (parts.length == 2) {
                int year = Integer.parseInt(parts[1]);
                int month = getMonthNumber(parts[0]);
                return LocalDate.of(year, month, Math.min(rules.getDayOfCycle(), 28));
            }
        }
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusDays(rules.getDayOfCycle() - 1);
    }

    private String getCurrentCycle(ChamaRulesEntity.CycleType cycleType) {
        LocalDate now = LocalDate.now();
        if (cycleType == ChamaRulesEntity.CycleType.MONTHLY) {
            return now.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        } else {
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
