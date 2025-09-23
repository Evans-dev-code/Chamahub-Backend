package com.example.loanmanagement.Contribution;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/contributions")
@Slf4j
public class ContributionController {

    @Autowired
    private ContributionService contributionService;

    @PostMapping
    public ResponseEntity<?> addContribution(
            @Valid @RequestBody ContributionDTO dto,
            Authentication auth) {
        try {
            log.info("Adding contribution for member {} in chama {}", dto.getMemberId(), dto.getChamaId());
            ContributionDTO result = contributionService.addContribution(dto);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Error adding contribution: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/chama/{chamaId}")
    public ResponseEntity<?> getContributionsByChama(
            @PathVariable Long chamaId,
            @RequestParam(required = false) String cycle) {
        try {
            log.info("Fetching contributions for chama {} and cycle {}", chamaId, cycle);
            List<ContributionDTO> contributions = contributionService.getContributionsByChama(chamaId, cycle);
            return ResponseEntity.ok(contributions);
        } catch (RuntimeException e) {
            log.error("Error fetching chama contributions: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getContributionsByMember(@PathVariable Long memberId) {
        try {
            log.info("Fetching contributions for member {}", memberId);
            List<ContributionDTO> contributions = contributionService.getContributionsByMember(memberId);
            return ResponseEntity.ok(contributions);
        } catch (RuntimeException e) {
            log.error("Error fetching member contributions: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/member/{memberId}/owed")
    public ResponseEntity<?> getOwedAmount(
            @PathVariable Long memberId,
            @RequestParam Long chamaId) {
        try {
            log.info("Calculating owed amount for member {} in chama {}", memberId, chamaId);
            ContributionOwedDTO owedInfo = contributionService.calculateOwedAmount(memberId, chamaId);
            return ResponseEntity.ok(owedInfo);
        } catch (RuntimeException e) {
            log.error("Error calculating owed amount: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/chama/{chamaId}/total")
    public ResponseEntity<?> getTotalContributions(
            @PathVariable Long chamaId,
            @RequestParam(required = false) String cycle) {
        try {
            BigDecimal total = contributionService.calculateTotalContributions(chamaId, cycle);
            return ResponseEntity.ok(total);
        } catch (RuntimeException e) {
            log.error("Error calculating total contributions: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/chama/{chamaId}/payout")
    public ResponseEntity<?> getNextPayout(@PathVariable Long chamaId) {
        try {
            log.info("Calculating next payout for chama {}", chamaId);
            MemberPayoutDTO payout = contributionService.calculateNextPayout(chamaId);
            return ResponseEntity.ok(payout);
        } catch (RuntimeException e) {
            log.error("Error calculating next payout: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/chama/{chamaId}/distribute-dividends")
    public ResponseEntity<?> distributeDividends(@PathVariable Long chamaId) {
        try {
            contributionService.distributeDividends(chamaId);
            return ResponseEntity.ok("Dividends distributed successfully");
        } catch (RuntimeException e) {
            log.error("Error distributing dividends: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/chama/{chamaId}/cycles")
    public ResponseEntity<?> getAvailableCycles(@PathVariable Long chamaId) {
        try {
            List<String> cycles = contributionService.getAvailableCycles(chamaId);
            return ResponseEntity.ok(cycles);
        } catch (RuntimeException e) {
            log.error("Error fetching available cycles: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}