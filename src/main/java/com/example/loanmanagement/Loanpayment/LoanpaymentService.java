package com.example.loanmanagement.Loanpayment;

import com.example.loanmanagement.Loanapplication.LoanApplicationEntity;
import com.example.loanmanagement.Loanapplication.LoanApplicationRepository;
import com.example.loanmanagement.Member.MemberEntity;
import com.example.loanmanagement.User.UserEntity;
import com.example.loanmanagement.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanpaymentService {

    @Autowired
    private LoanpaymentRepository paymentRepository;

    @Autowired
    private LoanApplicationRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    public LoanpaymentEntity makePayment(LoanpaymentDTO dto) {
        LoanApplicationEntity loan = loanRepository.findById(dto.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!"APPROVED".equalsIgnoreCase(loan.getStatus())) {
            throw new RuntimeException("Loan must be approved before payment");
        }

        UserEntity user = userRepository.findById(dto.getPaidByUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Ensure user belongs to the same chama as the loan
        Long loanChamaId = loan.getMember().getChama().getId();
        boolean isMemberOfChama = user.getMemberships().stream()
                .map(MemberEntity::getChama)
                .anyMatch(chama -> chama.getId().equals(loanChamaId));

        if (!isMemberOfChama) {
            throw new RuntimeException("User cannot pay for a loan outside their chama");
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

        return paymentRepository.save(payment);
    }

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

    // ✅ New service method to fetch payments by chama
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
