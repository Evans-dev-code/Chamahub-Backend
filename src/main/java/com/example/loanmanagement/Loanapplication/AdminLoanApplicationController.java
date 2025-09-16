package com.example.loanmanagement.Loanapplication;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/loan-applications")
public class AdminLoanApplicationController {

    private final LoanApplicationService loanService;

    public AdminLoanApplicationController(LoanApplicationService loanService) {
        this.loanService = loanService;
    }

    // ✅ Admin can fetch all loan applications for a specific chama
    @GetMapping("/chama/{chamaId}")
    public ResponseEntity<List<LoanApplicationDTO>> getLoansByChama(@PathVariable Long chamaId) {
        return ResponseEntity.ok(loanService.getLoansByChama(chamaId));
    }

    // ✅ Admin can update loan status (APPROVED / REJECTED / etc.)
    @PutMapping("/{id}/status")
    public ResponseEntity<LoanApplicationDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(loanService.updateLoanStatus(id, status));
    }

    // ✅ Admin can check status of a specific loan
    @GetMapping("/loan-status/{loanId}")
    public ResponseEntity<LoanStatusDTO> getAdminLoanStatus(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoanStatus(loanId));
    }
}
