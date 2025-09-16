package com.example.loanmanagement.Loanapplication;

public class LoanStatusDTO {
    private LoanApplicationEntity loan;
    private double totalPaid;
    private double outstandingBalance;

    public LoanStatusDTO(LoanApplicationEntity loan, double totalPaid, double outstandingBalance) {
        this.loan = loan;
        this.totalPaid = totalPaid;
        this.outstandingBalance = outstandingBalance;
    }

    public LoanApplicationEntity getLoan() {
        return loan;
    }

    public void setLoan(LoanApplicationEntity loan) {
        this.loan = loan;
    }

    public double getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(double totalPaid) {
        this.totalPaid = totalPaid;
    }

    public double getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(double outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }
}
