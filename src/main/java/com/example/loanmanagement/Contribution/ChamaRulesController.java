package com.example.loanmanagement.Contribution;

import com.example.loanmanagement.Member.MemberEntity;
import com.example.loanmanagement.Member.MemberRepository;
import com.example.loanmanagement.User.EmailService;
import com.example.loanmanagement.User.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chama-rules")
@Slf4j
public class ChamaRulesController {

    @Autowired
    private ChamaRulesService chamaRulesService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MemberRepository memberRepository; // ✅ use MemberRepo instead of UserRepo

    // ===== Create or update rules =====
    @PostMapping
    public ResponseEntity<?> createOrUpdateChamaRules(
            @Valid @RequestBody ChamaRulesDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            String role = extractRole(request);

            log.info("User {} with role {} creating/updating chama rules for chama {}", userId, role, dto.getChamaId());

            ChamaRulesDTO result = chamaRulesService.createOrUpdateChamaRules(dto);

            // ✅ Notify all members of this chama
            List<MemberEntity> members = memberRepository.findByChama_Id(dto.getChamaId());
            for (MemberEntity member : members) {
                UserEntity user = member.getUser();
                if (user != null && user.getEmail() != null) {
                    emailService.sendEmail(
                            user.getEmail(),
                            "Chama Rules Updated",
                            "Hello " + user.getFullName() + ",\n\nThe rules for your chama (ID: " +
                                    dto.getChamaId() + ") have been updated. Please log in to review.\n\n- ChamaHub Team"
                    );
                }
            }

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Error creating/updating chama rules: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ===== Delete rules =====
    @DeleteMapping("/chama/{chamaId}")
    public ResponseEntity<?> deleteChamaRules(@PathVariable Long chamaId, HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            String role = extractRole(request);

            log.info("User {} with role {} deleting chama rules for chama {}", userId, role, chamaId);

            chamaRulesService.deleteChamaRules(chamaId);

            // ✅ Notify all members
            List<MemberEntity> members = memberRepository.findByChama_Id(chamaId);
            for (MemberEntity member : members) {
                UserEntity user = member.getUser();
                if (user != null && user.getEmail() != null) {
                    emailService.sendEmail(
                            user.getEmail(),
                            "Chama Rules Deleted ⚠️",
                            "Hello " + user.getFullName() + ",\n\nThe rules for your chama (ID: " +
                                    chamaId + ") have been deleted.\n\n- ChamaHub Team"
                    );
                }
            }

            return ResponseEntity.ok("Chama rules deleted successfully");
        } catch (RuntimeException e) {
            log.error("Error deleting chama rules: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ===== Update payout order =====
    @PutMapping("/chama/{chamaId}/payout-order")
    public ResponseEntity<?> updatePayoutOrder(
            @PathVariable Long chamaId,
            @RequestBody String payoutOrder,
            HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            String role = extractRole(request);

            log.info("User {} with role {} updating payout order for chama {}", userId, role, chamaId);

            ChamaRulesDTO result = chamaRulesService.updatePayoutOrder(chamaId, payoutOrder);

            // ✅ Notify all members
            List<MemberEntity> members = memberRepository.findByChama_Id(chamaId);
            for (MemberEntity member : members) {
                UserEntity user = member.getUser();
                if (user != null && user.getEmail() != null) {
                    emailService.sendEmail(
                            user.getEmail(),
                            "Chama Payout Order Updated",
                            "Hello " + user.getFullName() + ",\n\nThe payout order for your chama (ID: " +
                                    chamaId + ") has been updated.\n\n- ChamaHub Team"
                    );
                }
            }

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Error updating payout order: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ===== Update current payout member =====
    @PutMapping("/chama/{chamaId}/current-payout-member")
    public ResponseEntity<?> updateCurrentPayoutMember(
            @PathVariable Long chamaId,
            @RequestParam Long memberId,
            HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            String role = extractRole(request);

            log.info("User {} with role {} updating current payout member for chama {} to member {}", userId, role, chamaId, memberId);

            ChamaRulesDTO result = chamaRulesService.updateCurrentPayoutMember(chamaId, memberId);

            // ✅ Notify that specific member (get UserEntity from MemberEntity)
            MemberEntity payoutMember = memberRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));

            UserEntity user = payoutMember.getUser();
            if (user != null && user.getEmail() != null) {
                emailService.sendEmail(
                        user.getEmail(),
                        "🎉 You Are Now the Payout Member",
                        "Hello " + user.getFullName() + ",\n\nYou have been set as the current payout member for chama (ID: " +
                                chamaId + "). Please log in to view details.\n\n- ChamaHub Team"
                );
            }

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Error updating current payout member: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ===== Helper methods =====
    private Long extractUserId(HttpServletRequest request) {
        Object obj = request.getAttribute("userId");
        if (obj == null) throw new RuntimeException("Unauthorized: userId missing");
        return obj instanceof Long ? (Long) obj : Long.parseLong(obj.toString());
    }

    private String extractRole(HttpServletRequest request) {
        Object obj = request.getAttribute("role");
        if (obj == null) throw new RuntimeException("Unauthorized: role missing");
        return obj.toString();
    }
}
