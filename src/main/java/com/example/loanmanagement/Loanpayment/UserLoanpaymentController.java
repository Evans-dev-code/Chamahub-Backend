package com.example.loanmanagement.Loanpayment;

import com.example.loanmanagement.User.UserEntity;
import com.example.loanmanagement.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/payments")
public class UserLoanpaymentController {

    private static final Logger logger = LoggerFactory.getLogger(UserLoanpaymentController.class);

    @Autowired
    private LoanpaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    // User makes a payment (matches frontend: POST /api/user/payments?chamaId=X)
    @PostMapping
    public ResponseEntity<?> payLoan(
            @RequestBody LoanpaymentDTO dto,
            @RequestParam Long chamaId,
            Authentication auth
    ) {
        String username = auth.getName();
        if (username == null) {
            return ResponseEntity.status(401).body("User authentication failed");
        }

        logger.info("User {} making payment for loan {} in chama {}", username, dto.getLoanId(), chamaId);

        try {
            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            dto.setPaidByAdmin(false);
            dto.setPaidByUserId(user.getId());

            // Use enhanced service method with chama validation
            LoanpaymentEntity savedPayment = paymentService.makePaymentWithChamaValidation(dto, username, chamaId);
            logger.info("Payment successful for user {}", username);
            return ResponseEntity.ok(new LoanpaymentDTO(savedPayment));
        } catch (RuntimeException e) {
            logger.error("Payment failed for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    // View payments made by the logged-in user in a specific chama
    @GetMapping("/my-payments")
    public ResponseEntity<?> getUserPayments(
            @RequestParam Long chamaId,
            Authentication auth
    ) {
        String username = auth.getName();
        if (username == null) {
            return ResponseEntity.status(401).body("User authentication failed");
        }

        logger.info("User {} requesting their payments in chama {}", username, chamaId);

        try {
            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<LoanpaymentDTO> payments = paymentService.getUserPaymentsInChama(user.getId(), chamaId)
                    .stream()
                    .map(LoanpaymentDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            logger.error("Error fetching user payments: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    // View payments for a specific loan (matches frontend: GET /api/user/payments/loan/X?chamaId=Y)
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<?> getPaymentsForLoan(
            @PathVariable Long loanId,
            @RequestParam Long chamaId,
            Authentication auth
    ) {
        String username = auth.getName();
        if (username == null) {
            return ResponseEntity.status(401).body("User authentication failed");
        }

        try {
            List<LoanpaymentDTO> payments = paymentService.getPaymentsForLoanWithAuth(loanId, username, chamaId)
                    .stream()
                    .map(LoanpaymentDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            logger.error("Error fetching loan payments: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    // Get total paid for a loan (matches frontend: GET /api/user/payments/loan/X/total-paid?chamaId=Y)
    @GetMapping("/loan/{loanId}/total-paid")
    public ResponseEntity<?> getTotalPaid(
            @PathVariable Long loanId,
            @RequestParam Long chamaId,
            Authentication auth
    ) {
        String username = auth.getName();
        if (username == null) {
            return ResponseEntity.status(401).body("User authentication failed");
        }

        try {
            double totalPaid = paymentService.getTotalPaidForLoanWithAuth(loanId, username, chamaId);
            return ResponseEntity.ok(totalPaid);
        } catch (RuntimeException e) {
            logger.error("Error fetching total paid: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    // Get outstanding balance for a loan
    @GetMapping("/outstanding/{loanId}")
    public ResponseEntity<?> getOutstandingBalance(
            @PathVariable Long loanId,
            @RequestParam Long chamaId,
            Authentication auth
    ) {
        String username = auth.getName();
        if (username == null) {
            return ResponseEntity.status(401).body("User authentication failed");
        }

        try {
            double balance = paymentService.getOutstandingBalanceWithAuth(loanId, username, chamaId);
            return ResponseEntity.ok(balance);
        } catch (RuntimeException e) {
            logger.error("Error fetching outstanding balance: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }
}