package com.example.loanmanagement.Loanapplication;

import com.example.loanmanagement.User.UserEntity;
import com.example.loanmanagement.User.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository loanRepo;
    private final UserRepository userRepo;

    public LoanApplicationService(LoanApplicationRepository loanRepo, UserRepository userRepo) {
        this.loanRepo = loanRepo;
        this.userRepo = userRepo;
    }

    public LoanApplicationDTO applyForLoan(LoanApplicationDTO dto, String username) {
        UserEntity user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LoanApplicationEntity loan = new LoanApplicationEntity();
        loan.setFullName(dto.fullName);
        loan.setEmail(dto.email);
        loan.setPhone(dto.phone);
        loan.setAmount(dto.amount);
        loan.setDuration(dto.duration);
        loan.setPurpose(dto.purpose);
        loan.setLoanType(dto.loanType);
        loan.setSalary(dto.salary);
        loan.setPersonalLoanInfo(dto.personalLoanInfo);
        loan.setMortgagePropertyValue(dto.mortgagePropertyValue);
        loan.setInterestRate(calculateRate(dto.loanType, dto.duration));
        loan.setTotalRepayment(calculateRepayment(dto.amount, loan.getInterestRate(), dto.duration));
        loan.setStatus("PENDING");
        loan.setApplicationDate(LocalDate.now());
        loan.setUser(user);

        loanRepo.save(loan);
        return mapToDTO(loan);
    }

    public List<LoanApplicationDTO> getUserApplications(String username) {
        UserEntity user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return loanRepo.findByUser(user).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<LoanApplicationDTO> getAllApplications() {
        return loanRepo.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public LoanApplicationDTO updateLoanStatus(Long id, String status) {
        LoanApplicationEntity loan = loanRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setStatus(status.toUpperCase());
        loanRepo.save(loan);
        return mapToDTO(loan);
    }

    private double calculateRate(String type, int duration) {
        return switch (type.toLowerCase()) {
            case "personal" -> duration > 12 ? 12 : 10;
            case "business" -> duration > 24 ? 15 : 13;
            case "mortgage" -> 6;
            case "auto" -> duration > 24 ? 9 : 7;
            default -> 10;
        };
    }

    private double calculateRepayment(double amount, double rate, int duration) {
        return amount + (amount * (rate / 100) * (duration / 12));
    }

    private LoanApplicationDTO mapToDTO(LoanApplicationEntity entity) {
        LoanApplicationDTO dto = new LoanApplicationDTO();
        dto.id = entity.getId();
        dto.fullName = entity.getFullName();
        dto.email = entity.getEmail();
        dto.phone = entity.getPhone();
        dto.amount = entity.getAmount();
        dto.duration = entity.getDuration();
        dto.purpose = entity.getPurpose();
        dto.loanType = entity.getLoanType();
        dto.salary = entity.getSalary();
        dto.personalLoanInfo = entity.getPersonalLoanInfo();
        dto.mortgagePropertyValue = entity.getMortgagePropertyValue();
        dto.interestRate = entity.getInterestRate();
        dto.totalRepayment = entity.getTotalRepayment();
        dto.status = entity.getStatus();
        dto.createdAt = entity.getApplicationDate().atStartOfDay();

        if (entity.getUser() != null) {
            dto.userId = entity.getUser().getId();
            dto.username = entity.getUser().getUsername();
        }

        return dto;
    }
}
