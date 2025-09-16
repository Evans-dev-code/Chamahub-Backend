package com.example.loanmanagement.Loanpayment;

import java.time.LocalDate;

public class LoanpaymentDTO {

    private Long loanId;
    private Long paidByUserId;
    private Double amountPaid;
    private boolean paidByAdmin;
    private LocalDate paymentDate;

    public LoanpaymentDTO() {}

    public LoanpaymentDTO(Long loanId, Long paidByUserId, Double amountPaid, boolean paidByAdmin, LocalDate paymentDate) {
        this.loanId = loanId;
        this.paidByUserId = paidByUserId;
        this.amountPaid = amountPaid;
        this.paidByAdmin = paidByAdmin;
        this.paymentDate = paymentDate;
    }

    // Map entity → DTO
    public LoanpaymentDTO(LoanpaymentEntity entity) {
        this.loanId = entity.getLoan().getId();
        this.paidByUserId = entity.getPaidBy().getId();
        this.amountPaid = entity.getAmountPaid();
        this.paidByAdmin = entity.isPaidByAdmin();
        this.paymentDate = entity.getPaymentDate();
    }

    // --- Getters and Setters ---
    public Long getLoanId() { return loanId; }
    public void setLoanId(Long loanId) { this.loanId = loanId; }

    public Long getPaidByUserId() { return paidByUserId; }
    public void setPaidByUserId(Long paidByUserId) { this.paidByUserId = paidByUserId; }

    public Double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Double amountPaid) { this.amountPaid = amountPaid; }

    public boolean isPaidByAdmin() { return paidByAdmin; }
    public void setPaidByAdmin(boolean paidByAdmin) { this.paidByAdmin = paidByAdmin; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
}
