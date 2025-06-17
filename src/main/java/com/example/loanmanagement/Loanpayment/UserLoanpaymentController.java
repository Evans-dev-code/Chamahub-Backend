package com.example.loanmanagement.Loanpayment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/payments")
public class UserLoanpaymentController {

    @Autowired
    private LoanpaymentService paymentService;

    // 🔵 User makes a payment
    @PostMapping
    public LoanpaymentEntity payLoan(@RequestBody LoanpaymentDTO dto) {
        dto.setPaidByAdmin(false); // Users can't pay as admin
        return paymentService.makePayment(dto);
    }

    // 🔍 View payments made by user
    @GetMapping("/by-user/{userId}")
    public List<LoanpaymentEntity> getUserPayments(@PathVariable Long userId) {
        return paymentService.getPaymentsByUser(userId);
    }

    // 💰 View payments for a specific loan
    @GetMapping("/by-loan/{loanId}")
    public List<LoanpaymentEntity> getPaymentsForLoan(@PathVariable Long loanId) {
        return paymentService.getPaymentsForLoan(loanId);
    }

    // 📉 Get outstanding balance
    @GetMapping("/outstanding/{loanId}")
    public double getOutstandingBalance(@PathVariable Long loanId) {
        return paymentService.getOutstandingBalance(loanId);
    }
}
