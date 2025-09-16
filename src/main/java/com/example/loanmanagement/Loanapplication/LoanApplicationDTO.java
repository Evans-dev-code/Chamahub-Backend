package com.example.loanmanagement.Loanapplication;

import java.time.LocalDateTime;

public class LoanApplicationDTO {
    public Long id;
    public Long memberId;
    public Long chamaId;
    public String fullName;
    public String email;
    public String phone;
    public Double amount;
    public Integer duration;
    public String purpose;
    public String loanType;
    public Double salary;
    public String personalLoanInfo;
    public Double mortgagePropertyValue;
    public Double interestRate;
    public Double totalRepayment;
    public String status;
    public LocalDateTime createdAt;

    public String username;
    public Double remainingBalance;
}

