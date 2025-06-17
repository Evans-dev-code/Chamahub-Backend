package com.example.loanmanagement.Loanpayment;

import com.example.loanmanagement.Loanapplication.LoanApplicationEntity;
import com.example.loanmanagement.Loanapplication.LoanApplicationRepository;
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

        double totalPaid = paymentRepository.findByLoan(loan).stream()
                .mapToDouble(LoanpaymentEntity::getAmountPaid)
                .sum();

        if ((totalPaid + dto.getAmountPaid()) > (loan.getTotalRepayment() != null ? loan.getTotalRepayment() : 0.0)) {
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

    public double getOutstandingBalance(Long loanId) {
        LoanApplicationEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        double totalPaid = paymentRepository.findByLoan(loan).stream()
                .mapToDouble(LoanpaymentEntity::getAmountPaid)
                .sum();

        return (loan.getTotalRepayment() != null ? loan.getTotalRepayment() : 0.0) - totalPaid;
    }
}
