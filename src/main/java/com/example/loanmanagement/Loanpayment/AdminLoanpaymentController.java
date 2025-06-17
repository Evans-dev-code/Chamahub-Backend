package com.example.loanmanagement.Loanpayment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminLoanpaymentController {

    @Autowired
    private LoanpaymentService paymentService;

    // 🔴 Admin pays manually for a user's loan
    @PostMapping
    public LoanpaymentEntity adminPayLoan(@RequestBody LoanpaymentDTO dto) {
        dto.setPaidByAdmin(true);
        return paymentService.makePayment(dto);
    }
}
