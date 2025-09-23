package com.example.loanmanagement.Contribution;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chama-rules")
@Slf4j
public class ChamaRulesController {

    @Autowired
    private ChamaRulesService chamaRulesService;

    @PostMapping
    public ResponseEntity<?> createOrUpdateChamaRules(
            @Valid @RequestBody ChamaRulesDTO dto,
            Authentication auth) {
        try {
            log.info("Creating/updating chama rules for chama {}", dto.getChamaId());
            ChamaRulesDTO result = chamaRulesService.createOrUpdateChamaRules(dto);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Error creating/updating chama rules: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/chama/{chamaId}")
    public ResponseEntity<?> getChamaRules(@PathVariable Long chamaId) {
        try {
            log.info("Fetching chama rules for chama {}", chamaId);
            ChamaRulesDTO rules = chamaRulesService.getChamaRules(chamaId);
            return ResponseEntity.ok(rules);
        } catch (RuntimeException e) {
            log.error("Error fetching chama rules: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllChamaRules() {
        try {
            log.info("Fetching all chama rules");
            List<ChamaRulesDTO> allRules = chamaRulesService.getAllChamaRules();
            return ResponseEntity.ok(allRules);
        } catch (RuntimeException e) {
            log.error("Error fetching all chama rules: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/chama/{chamaId}")
    public ResponseEntity<?> deleteChamaRules(@PathVariable Long chamaId) {
        try {
            log.info("Deleting chama rules for chama {}", chamaId);
            chamaRulesService.deleteChamaRules(chamaId);
            return ResponseEntity.ok("Chama rules deleted successfully");
        } catch (RuntimeException e) {
            log.error("Error deleting chama rules: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/chama/{chamaId}/payout-order")
    public ResponseEntity<?> updatePayoutOrder(
            @PathVariable Long chamaId,
            @RequestBody String payoutOrder) {
        try {
            log.info("Updating payout order for chama {}", chamaId);
            ChamaRulesDTO result = chamaRulesService.updatePayoutOrder(chamaId, payoutOrder);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Error updating payout order: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/chama/{chamaId}/current-payout-member")
    public ResponseEntity<?> updateCurrentPayoutMember(
            @PathVariable Long chamaId,
            @RequestParam Long memberId) {
        try {
            log.info("Updating current payout member for chama {} to member {}", chamaId, memberId);
            ChamaRulesDTO result = chamaRulesService.updateCurrentPayoutMember(chamaId, memberId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Error updating current payout member: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/chama/{chamaId}/exists")
    public ResponseEntity<?> checkChamaRulesExist(@PathVariable Long chamaId) {
        try {
            boolean exists = chamaRulesService.chamaRulesExist(chamaId);
            return ResponseEntity.ok(exists);
        } catch (RuntimeException e) {
            log.error("Error checking if chama rules exist: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}