package com.example.loanmanagement.Loanpayment;

import com.example.loanmanagement.Loanapplication.LoanApplicationEntity;
import com.example.loanmanagement.Loanapplication.LoanApplicationRepository;
import com.example.loanmanagement.Member.MemberEntity;
import com.example.loanmanagement.User.UserEntity;
import com.example.loanmanagement.User.UserRepository;
import com.example.loanmanagement.Chama.ChamaEntity;
import com.example.loanmanagement.Chama.ChamaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanpaymentService {

    private static final Logger logger = LoggerFactory.getLogger(LoanpaymentService.class);

    @Autowired
    private LoanpaymentRepository paymentRepository;

    @Autowired
    private LoanApplicationRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChamaRepository chamaRepository;

    // ✅ User payment with chama validation
    public LoanpaymentEntity makePaymentWithChamaValidation(LoanpaymentDTO dto, String username, Long chamaId) {
        logger.info("User {} making payment for loan {} in chama {}", username, dto.getLoanId(), chamaId);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LoanApplicationEntity loan = loanRepository.findById(dto.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getMember().getChama().getId().equals(chamaId)) {
            throw new RuntimeException("Loan does not belong to the specified chama");
        }

        boolean isMemberOfChama = user.getMemberships().stream()
                .map(MemberEntity::getChama)
                .anyMatch(chama -> chama.getId().equals(chamaId));

        if (!isMemberOfChama) {
            throw new RuntimeException("You are not a member of this chama");
        }

        return processPayment(dto, loan, user);
    }

    // ✅ Admin payment with chama membership role validation
    public LoanpaymentEntity makeAdminPaymentWithChamaValidation(LoanpaymentDTO dto, String adminUsername, Long chamaId) {
        logger.info("Admin {} making payment for loan {} in chama {}", adminUsername, dto.getLoanId(), chamaId);

        UserEntity admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        LoanApplicationEntity loan = loanRepository.findById(dto.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getMember().getChama().getId().equals(chamaId)) {
            throw new RuntimeException("Loan does not belong to the specified chama");
        }

        boolean isAdmin = admin.getMemberships().stream()
                .anyMatch(m -> m.getChama().getId().equals(chamaId) &&
                        m.getChamaRole().name().equals("ADMIN"));

        if (!isAdmin) {
            throw new RuntimeException("You are not an admin of this chama");
        }

        return processPayment(dto, loan, admin);
    }

    // ✅ Original method (backward compatibility)
    public LoanpaymentEntity makePayment(LoanpaymentDTO dto) {
        LoanApplicationEntity loan = loanRepository.findById(dto.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!"APPROVED".equalsIgnoreCase(loan.getStatus())) {
            throw new RuntimeException("Loan must be approved before payment");
        }

        UserEntity user = userRepository.findById(dto.getPaidByUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long loanChamaId = loan.getMember().getChama().getId();
        boolean isMemberOfChama = user.getMemberships().stream()
                .map(MemberEntity::getChama)
                .anyMatch(chama -> chama.getId().equals(loanChamaId));

        if (!isMemberOfChama) {
            throw new RuntimeException("User cannot pay for a loan outside their chama");
        }

        return processPayment(dto, loan, user);
    }

    // ✅ Common payment processing
    private LoanpaymentEntity processPayment(LoanpaymentDTO dto, LoanApplicationEntity loan, UserEntity user) {
        if (!"APPROVED".equalsIgnoreCase(loan.getStatus())) {
            throw new RuntimeException("Loan must be approved before payment");
        }

        double totalPaid = paymentRepository.findByLoan(loan).stream()
                .mapToDouble(LoanpaymentEntity::getAmountPaid)
                .sum();

        double newTotal = totalPaid + dto.getAmountPaid();
        if (newTotal > (loan.getTotalRepayment() != null ? loan.getTotalRepayment() : 0.0)) {
            throw new RuntimeException("Payment exceeds loan repayment amount");
        }

        LoanpaymentEntity payment = new LoanpaymentEntity();
        payment.setLoan(loan);
        payment.setPaidBy(user);
        payment.setAmountPaid(dto.getAmountPaid());
        payment.setPaidByAdmin(dto.isPaidByAdmin());
        payment.setPaymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : java.time.LocalDate.now());

        LoanpaymentEntity saved = paymentRepository.save(payment);
        logger.info("Payment saved successfully with ID: {}", saved.getId());
        return saved;
    }

    // ✅ Get user payments in chama
    public List<LoanpaymentEntity> getUserPaymentsInChama(Long userId, Long chamaId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isMemberOfChama = user.getMemberships().stream()
                .map(MemberEntity::getChama)
                .anyMatch(chama -> chama.getId().equals(chamaId));

        if (!isMemberOfChama) {
            throw new RuntimeException("You are not a member of this chama");
        }

        return paymentRepository.findByPaidBy(user).stream()
                .filter(payment -> payment.getLoan().getMember().getChama().getId().equals(chamaId))
                .collect(Collectors.toList());
    }

    // ✅ Get payments by chama with admin authorization
    public List<LoanpaymentEntity> getPaymentsByChamaWithAuth(Long chamaId, String adminUsername) {
        UserEntity admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        ChamaEntity chama = chamaRepository.findById(chamaId)
                .orElseThrow(() -> new RuntimeException("Chama not found"));

        boolean isAdmin = admin.getMemberships().stream()
                .anyMatch(m -> m.getChama().getId().equals(chama.getId()) &&
                        m.getChamaRole().name().equals("ADMIN"));

        if (!isAdmin) {
            throw new RuntimeException("You are not an admin of this chama");
        }

        return paymentRepository.findByLoan_Member_Chama_Id(chamaId);
    }

    // ✅ Get payments for loan with user authorization
    public List<LoanpaymentEntity> getPaymentsForLoanWithAuth(Long loanId, String username, Long chamaId) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LoanApplicationEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getMember().getChama().getId().equals(chamaId)) {
            throw new RuntimeException("Loan does not belong to the specified chama");
        }

        boolean isMemberOfChama = user.getMemberships().stream()
                .map(MemberEntity::getChama)
                .anyMatch(chama -> chama.getId().equals(chamaId));

        if (!isMemberOfChama) {
            throw new RuntimeException("You are not a member of this chama");
        }

        return paymentRepository.findByLoan(loan);
    }

    // ✅ Get admin payments for loan with role validation
    public List<LoanpaymentEntity> getAdminPaymentsForLoan(Long loanId, String adminUsername, Long chamaId) {
        UserEntity admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        LoanApplicationEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getMember().getChama().getId().equals(chamaId)) {
            throw new RuntimeException("Loan does not belong to the specified chama");
        }

        boolean isAdmin = admin.getMemberships().stream()
                .anyMatch(m -> m.getChama().getId().equals(chamaId) &&
                        m.getChamaRole().name().equals("ADMIN"));

        if (!isAdmin) {
            throw new RuntimeException("You are not an admin of this chama");
        }

        return paymentRepository.findByLoan(loan);
    }

    // ✅ Get totals with auth
    public double getTotalPaidForLoanWithAuth(Long loanId, String username, Long chamaId) {
        getPaymentsForLoanWithAuth(loanId, username, chamaId);
        return getTotalPaidForLoan(loanId);
    }

    public double getOutstandingBalanceWithAuth(Long loanId, String username, Long chamaId) {
        getPaymentsForLoanWithAuth(loanId, username, chamaId);
        return getOutstandingBalance(loanId);
    }

    // ✅ Existing utility methods
    public List<LoanpaymentEntity> getPaymentsForLoan(Long loanId) {
        LoanApplicationEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        return paymentRepository.findByLoan(loan);
    }

    public List<LoanpaymentEntity> getPaymentsByUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return paymentRepository.findByPaidBy(user);
    }

    public List<LoanpaymentEntity> getPaymentsByChama(Long chamaId) {
        return paymentRepository.findByLoan_Member_Chama_Id(chamaId);
    }

    public double getOutstandingBalance(Long loanId) {
        LoanApplicationEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        double totalPaid = paymentRepository.findByLoan(loan).stream()
                .mapToDouble(LoanpaymentEntity::getAmountPaid)
                .sum();

        double totalRepayment = loan.getTotalRepayment() != null ? loan.getTotalRepayment() : 0.0;
        return totalRepayment - totalPaid;
    }

    public double getTotalPaidForLoan(Long loanId) {
        LoanApplicationEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        return paymentRepository.findByLoan(loan).stream()
                .mapToDouble(LoanpaymentEntity::getAmountPaid)
                .sum();
    }
}
