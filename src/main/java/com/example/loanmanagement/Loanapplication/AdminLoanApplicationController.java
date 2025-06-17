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

    @GetMapping
    public ResponseEntity<List<LoanApplicationDTO>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllApplications());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LoanApplicationDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(loanService.updateLoanStatus(id, status));
    }
}
