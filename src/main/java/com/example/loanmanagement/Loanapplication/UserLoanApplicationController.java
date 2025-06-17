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

    @PostMapping
    public ResponseEntity<LoanApplicationDTO> applyLoan(
            @RequestBody LoanApplicationDTO dto,
            Authentication auth
    ) {
        String username = auth.getName();
        return ResponseEntity.ok(loanService.applyForLoan(dto, username));
    }

    @GetMapping
    public ResponseEntity<List<LoanApplicationDTO>> getMyLoans(Authentication auth) {
        String username = auth.getName();
        return ResponseEntity.ok(loanService.getUserApplications(username));
    }
}
