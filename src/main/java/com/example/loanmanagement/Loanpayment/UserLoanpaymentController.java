package com.example.loanmanagement.Loanpayment;

import com.example.loanmanagement.User.UserEntity;
import com.example.loanmanagement.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/payments")
public class UserLoanpaymentController {

    @Autowired
    private LoanpaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    // User makes a payment
    @PostMapping
    public LoanpaymentDTO payLoan(@RequestBody LoanpaymentDTO dto, Authentication auth) {
        String username = auth.getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        dto.setPaidByAdmin(false);
        dto.setPaidByUserId(user.getId());

        LoanpaymentEntity savedPayment = paymentService.makePayment(dto);
        return new LoanpaymentDTO(savedPayment);
    }

    // View payments made by the logged-in user
    @GetMapping("/my-payments")
    public List<LoanpaymentDTO> getUserPayments(Authentication auth) {
        String username = auth.getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return paymentService.getPaymentsByUser(user.getId())
                .stream()
                .map(LoanpaymentDTO::new)
                .collect(Collectors.toList());
    }

    // View payments for a specific loan
    @GetMapping("/by-loan/{loanId}")
    public List<LoanpaymentDTO> getPaymentsForLoan(@PathVariable Long loanId) {
        return paymentService.getPaymentsForLoan(loanId)
                .stream()
                .map(LoanpaymentDTO::new)
                .collect(Collectors.toList());
    }

    // Get outstanding balance for a loan
    @GetMapping("/outstanding/{loanId}")
    public double getOutstandingBalance(@PathVariable Long loanId) {
        return paymentService.getOutstandingBalance(loanId);
    }

    // Get total paid for a loan
    @GetMapping("/loan/{loanId}/total-paid")
    public double getTotalPaid(@PathVariable Long loanId) {
        return paymentService.getTotalPaidForLoan(loanId);
    }
}
