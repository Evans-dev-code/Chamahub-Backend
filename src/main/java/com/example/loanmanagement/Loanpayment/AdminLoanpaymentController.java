package com.example.loanmanagement.Loanpayment;

import com.example.loanmanagement.User.UserEntity;
import com.example.loanmanagement.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminLoanpaymentController {

    @Autowired
    private LoanpaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    // Admin pays manually for a user's loan
    @PostMapping
    public LoanpaymentDTO adminPayLoan(@RequestBody LoanpaymentDTO dto, Authentication auth) {
        String username = auth.getName();
        UserEntity admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        dto.setPaidByAdmin(true);
        dto.setPaidByUserId(admin.getId());

        LoanpaymentEntity savedPayment = paymentService.makePayment(dto);
        return new LoanpaymentDTO(savedPayment);
    }

    // ✅ Admin fetches all payments for a chama
    @GetMapping("/chama/{chamaId}")
    public List<LoanpaymentDTO> getPaymentsForChama(@PathVariable Long chamaId) {
        return paymentService.getPaymentsByChama(chamaId)
                .stream()
                .map(LoanpaymentDTO::new)
                .collect(Collectors.toList());
    }
}
