package com.example.loanmanagement.Loanapplication;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/loan-applications")
public class UserLoanApplicationController {

    private final LoanApplicationService loanService;

    public UserLoanApplicationController(LoanApplicationService loanService) {
        this.loanService = loanService;
    }

    // ✅ User applies for a loan within a specific chama
    @PostMapping("/chama/{chamaId}")
    public ResponseEntity<LoanApplicationDTO> applyLoan(
            @RequestBody LoanApplicationDTO dto,
            @PathVariable Long chamaId,
            Authentication auth
    ) {
        String username = auth.getName();
        return ResponseEntity.ok(loanService.applyForLoan(dto, username, chamaId));
    }

    // ✅ User fetches their own loan applications for a chama
    @GetMapping("/chama/{chamaId}")
    public ResponseEntity<List<LoanApplicationDTO>> getMyLoans(
            @PathVariable Long chamaId,
            Authentication auth
    ) {
        String username = auth.getName();
        return ResponseEntity.ok(loanService.getUserApplications(username, chamaId));
    }

    // ✅ User checks status of a specific loan
    @GetMapping("/loan-status/{loanId}")
    public ResponseEntity<LoanStatusDTO> getUserLoanStatus(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoanStatus(loanId));
    }
}
