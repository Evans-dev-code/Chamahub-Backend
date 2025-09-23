package com.example.loanmanagement.Loanapplication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/user/loan-applications")
public class UserLoanApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(UserLoanApplicationController.class);

    private final LoanApplicationService loanService;

    public UserLoanApplicationController(LoanApplicationService loanService) {
        this.loanService = loanService;
    }

    // ✅ User applies for a loan within a specific chama
    @PostMapping
    public ResponseEntity<?> applyLoan(
            @RequestBody LoanApplicationDTO dto,
            @RequestParam Long chamaId,
            Authentication auth
    ) {
        String username = auth.getName();

        if (username == null) {
            logger.error("❌ Username not found in authentication");
            return ResponseEntity.status(401).body("User authentication failed");
        }

        logger.info("🔍 User {} applying for loan in chama {}", username, chamaId);

        try {
            LoanApplicationDTO result = loanService.applyForLoan(dto, username, chamaId);
            logger.info("✅ Loan application submitted successfully for user {}", username);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            logger.error("❌ Error applying for loan: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    // ✅ User fetches their own loan applications for a chama
    @GetMapping
    public ResponseEntity<?> getMyLoans(
            @RequestParam Long chamaId,
            Authentication auth
    ) {
        String username = auth.getName();

        if (username == null) {
            logger.error("❌ Username not found in authentication");
            return ResponseEntity.status(401).body("User authentication failed");
        }

        logger.info("🔍 User {} requesting loan applications for chama {}", username, chamaId);

        try {
            List<LoanApplicationDTO> results = loanService.getUserApplications(username, chamaId);
            logger.info("✅ Found {} loan applications for user {}", results.size(), username);
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            logger.error("❌ Error fetching loan applications: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    // ✅ User checks status of a specific loan
    @GetMapping("/loan-status/{loanId}")
    public ResponseEntity<?> getUserLoanStatus(@PathVariable Long loanId) {
        try {
            LoanStatusDTO status = loanService.getLoanStatus(loanId);
            return ResponseEntity.ok(status);
        } catch (RuntimeException e) {
            logger.error("❌ Error fetching loan status: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }
}