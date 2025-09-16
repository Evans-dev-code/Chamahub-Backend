package com.example.loanmanagement.Loanpayment;

import com.example.loanmanagement.Loanapplication.LoanApplicationEntity;
import com.example.loanmanagement.User.UserEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "loan_payments")
public class LoanpaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which loan this payment belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private LoanApplicationEntity loan;

    // Who paid (UserEntity - could be admin or borrower)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false)
    private UserEntity paidBy;

    // Payment amount
    @Column(nullable = false)
    private Double amountPaid;

    // Date the payment was made
    @Column(nullable = false)
    private LocalDate paymentDate;

    // Was it manually paid by admin?
    @Column(nullable = false)
    private boolean paidByAdmin;

    public LoanpaymentEntity() {
        this.paymentDate = LocalDate.now();
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LoanApplicationEntity getLoan() { return loan; }
    public void setLoan(LoanApplicationEntity loan) { this.loan = loan; }

    public UserEntity getPaidBy() { return paidBy; }
    public void setPaidBy(UserEntity paidBy) { this.paidBy = paidBy; }

    public Double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Double amountPaid) { this.amountPaid = amountPaid; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public boolean isPaidByAdmin() { return paidByAdmin; }
    public void setPaidByAdmin(boolean paidByAdmin) { this.paidByAdmin = paidByAdmin; }
}
