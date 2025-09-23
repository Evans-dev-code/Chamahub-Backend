package com.example.loanmanagement.Loanapplication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/admin/loan-applications")
public class AdminLoanApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(AdminLoanApplicationController.class);

    private final LoanApplicationService loanService;

    public AdminLoanApplicationController(LoanApplicationService loanService) {
        this.loanService = loanService;
    }

    // ✅ Admin can fetch all loan applications for a specific chama
    @GetMapping
    public ResponseEntity<?> getLoansByChama(@RequestParam Long chamaId) {
        logger.info("🔍 Admin requesting loans for chama {}", chamaId);

        try {
            List<LoanApplicationDTO> loans = loanService.getLoansByChama(chamaId);
            logger.info("✅ Found {} loans for chama {}", loans.size(), chamaId);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            logger.error("❌ Error fetching loans for chama: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    // ✅ Admin can update loan status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam Long chamaId
    ) {
        logger.info("🔍 Admin updating loan {} status to {} in chama {}", id, status, chamaId);

        try {
            LoanApplicationDTO updatedLoan = loanService.updateLoanStatus(id, status);
            logger.info("✅ Loan {} status updated to {}", id, status);
            return ResponseEntity.ok(updatedLoan);
        } catch (Exception e) {
            logger.error("❌ Error updating loan status: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    // ✅ Admin can check status of a specific loan
    @GetMapping("/loan-status/{loanId}")
    public ResponseEntity<?> getAdminLoanStatus(@PathVariable Long loanId) {
        try {
            LoanStatusDTO status = loanService.getLoanStatus(loanId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("❌ Error fetching loan status: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }
}