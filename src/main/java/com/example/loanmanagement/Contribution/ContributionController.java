package com.example.loanmanagement.Contribution;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/contributions")
@Slf4j
public class ContributionController {

    @Autowired
    private ContributionService contributionService;

    // ===== Add a new contribution =====
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> addContribution(@Valid @RequestBody ContributionDTO dto, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) throw new RuntimeException("Unauthorized: userId missing");

            ContributionDTO result = contributionService.addContribution(dto, userId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Error adding contribution: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ===== Get contributions for a member =====
    @GetMapping("/member")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getContributionsByMember(
            @RequestParam(required = false) Long memberId,
            @RequestParam Long chamaId,
            @RequestParam(required = false) String cycle,
            HttpServletRequest request) {

        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) throw new RuntimeException("Unauthorized: userId missing");

            // If the user is not an admin, only allow fetching their own contributions
            boolean isAdmin = request.isUserInRole("ADMIN");
            if (!isAdmin) memberId = userId;
            else if (memberId == null) return ResponseEntity.badRequest().body("Admin must provide memberId");

            List<ContributionDTO> contributions = contributionService.getContributionsByMember(memberId, chamaId, cycle);
            return ResponseEntity.ok(contributions);
        } catch (RuntimeException e) {
            log.error("Error fetching member contributions: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ===== Get owed amount =====
    @GetMapping("/member/owed")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getOwedAmount(
            @RequestParam(required = false) Long memberId,
            @RequestParam Long chamaId,
            HttpServletRequest request) {

        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) throw new RuntimeException("Unauthorized: userId missing");

            boolean isAdmin = request.isUserInRole("ADMIN");
            if (!isAdmin) memberId = userId;
            else if (memberId == null) return ResponseEntity.badRequest().body("Admin must provide memberId");

            ContributionOwedDTO owedInfo = contributionService.calculateOwedAmount(memberId, chamaId);
            return ResponseEntity.ok(owedInfo);
        } catch (RuntimeException e) {
            log.error("Error calculating owed amount: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ===== Chama contributions & totals =====
    @GetMapping("/chama/{chamaId}")
    public ResponseEntity<?> getContributionsByChama(@PathVariable Long chamaId,
                                                     @RequestParam(required = false) String cycle) {
        try {
            List<ContributionDTO> contributions = contributionService.getContributionsByChama(chamaId, cycle);
            return ResponseEntity.ok(contributions);
        } catch (RuntimeException e) {
            log.error("Error fetching chama contributions: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/chama/{chamaId}/total")
    public ResponseEntity<?> getTotalContributions(@PathVariable Long chamaId,
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
