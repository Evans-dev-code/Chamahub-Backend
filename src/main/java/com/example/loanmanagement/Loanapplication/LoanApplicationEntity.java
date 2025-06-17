package com.example.loanmanagement.Loanapplication;

import com.example.loanmanagement.User.UserEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "loan_applications")
public class LoanApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String phone;

    private Double amount;
    private int duration;
    private String purpose;
    private String loanType;

    private Double salary;
    private String personalLoanInfo;
    private Double mortgagePropertyValue;

    private Double interestRate;
    private Double totalRepayment;

    private String status;

    private LocalDate applicationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public LoanApplicationEntity() {
        this.status = "PENDING";
        this.applicationDate = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getPersonalLoanInfo() {
        return personalLoanInfo;
    }

    public void setPersonalLoanInfo(String personalLoanInfo) {
        this.personalLoanInfo = personalLoanInfo;
    }

    public Double getMortgagePropertyValue() {
        return mortgagePropertyValue;
    }

    public void setMortgagePropertyValue(Double mortgagePropertyValue) {
        this.mortgagePropertyValue = mortgagePropertyValue;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Double getTotalRepayment() {
        return totalRepayment;
    }

    public void setTotalRepayment(Double totalRepayment) {
        this.totalRepayment = totalRepayment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
