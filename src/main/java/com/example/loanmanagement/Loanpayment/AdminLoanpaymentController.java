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
@RequestMapping("/api/admin/payments")
public class AdminLoanpaymentController {

    private static final Logger logger = LoggerFactory.getLogger(AdminLoanpaymentController.class);

    @Autowired
    private LoanpaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    // Admin pays manually for a user's loan (matches frontend: POST /api/admin/payments?chamaId=X)
    @PostMapping
    public ResponseEntity<?> adminPayLoan(
            @RequestBody LoanpaymentDTO dto,
            @RequestParam Long chamaId,
            Authentication auth
    ) {
        String username = auth.getName();
        if (username == null) {
            return ResponseEntity.status(401).body("Admin authentication failed");
        }

        logger.info("Admin {} making payment for loan {} in chama {}", username, dto.getLoanId(), chamaId);

        try {
            UserEntity admin = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            dto.setPaidByAdmin(true);
            dto.setPaidByUserId(admin.getId());

            // Use enhanced service method with admin chama validation
            LoanpaymentEntity savedPayment = paymentService.makeAdminPaymentWithChamaValidation(dto, username, chamaId);
            logger.info("Admin payment successful for admin {}", username);
            return ResponseEntity.ok(new LoanpaymentDTO(savedPayment));
        } catch (RuntimeException e) {
            logger.error("Admin payment failed for admin {}: {}", username, e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    // Admin fetches all payments for their chama (matches frontend: GET /api/admin/payments?chamaId=X)
    @GetMapping
    public ResponseEntity<?> getPaymentsForChama(
            @RequestParam Long chamaId,
            Authentication auth
    ) {
        String username = auth.getName();
        if (username == null) {
            return ResponseEntity.status(401).body("Admin authentication failed");
        }

        logger.info("Admin {} requesting all payments for chama {}", username, chamaId);

        try {
            List<LoanpaymentDTO> payments = paymentService.getPaymentsByChamaWithAuth(chamaId, username)
                    .stream()
                    .map(LoanpaymentDTO::new)
                    .collect(Collectors.toList());

            logger.info("Found {} payments for chama {}", payments.size(), chamaId);
            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            logger.error("Error fetching chama payments: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    // Admin fetches payments for a specific loan (matches frontend: GET /api/admin/payments/loan/X?chamaId=Y)
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<?> getPaymentsForLoan(
            @PathVariable Long loanId,
            @RequestParam Long chamaId,
            Authentication auth
    ) {
        String username = auth.getName();
        if (username == null) {
            return ResponseEntity.status(401).body("Admin authentication failed");
        }

        logger.info("Admin {} requesting payments for loan {} in chama {}", username, loanId, chamaId);

        try {
            List<LoanpaymentDTO> payments = paymentService.getAdminPaymentsForLoan(loanId, username, chamaId)
                    .stream()
                    .map(LoanpaymentDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            logger.error("Error fetching loan payments for admin: {}", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }
}