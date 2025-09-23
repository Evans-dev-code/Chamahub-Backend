package com.example.loanmanagement.Loanapplication;

import com.example.loanmanagement.Loanpayment.LoanpaymentService;
import com.example.loanmanagement.Member.MemberEntity;
import com.example.loanmanagement.Member.MemberRepository;
import com.example.loanmanagement.User.UserEntity;
import com.example.loanmanagement.User.UserRepository;
import com.example.loanmanagement.Chama.ChamaEntity;
import com.example.loanmanagement.Chama.ChamaRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationService.class);

    private final LoanApplicationRepository loanRepo;
    private final UserRepository userRepo;
    private final MemberRepository memberRepo;
    private final LoanpaymentService paymentService;
    private final ChamaRepository chamaRepo; // Added for admin validation

    public LoanApplicationService(
            LoanApplicationRepository loanRepo,
            UserRepository userRepo,
            MemberRepository memberRepo,
            LoanpaymentService paymentService,
            ChamaRepository chamaRepo) {
        this.loanRepo = loanRepo;
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
        this.paymentService = paymentService;
        this.chamaRepo = chamaRepo;
    }

    // ✅ Member applies for a loan in a specific chama
    public LoanApplicationDTO applyForLoan(LoanApplicationDTO dto, String username, Long chamaId) {
        logger.info("📝 User {} applying for loan in chama {}", username, chamaId);

        UserEntity user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔑 Since user can belong to multiple chamas, find correct membership
        MemberEntity member = memberRepo.findAllByUser(user).stream()
                .filter(m -> m.getChama().getId().equals(chamaId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User is not a member of this chama"));

        logger.info("✅ User {} is member of chama {}", username, chamaId);

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
        loan.setMember(member);

        loanRepo.save(loan);
        logger.info("✅ Loan application saved with ID: {}", loan.getId());
        return mapToDTO(loan);
    }

    // ✅ Get user's own loans in a chama
    public List<LoanApplicationDTO> getUserApplications(String username, Long chamaId) {
        logger.info("📋 Fetching loan applications for user {} in chama {}", username, chamaId);

        UserEntity user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MemberEntity member = memberRepo.findAllByUser(user).stream()
                .filter(m -> m.getChama().getId().equals(chamaId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User is not part of this chama"));

        List<LoanApplicationDTO> loans = loanRepo.findByMember(member)
                .stream().map(this::mapToDTO).collect(Collectors.toList());

        logger.info("✅ Found {} loan applications for user {}", loans.size(), username);
        return loans;
    }

    // ✅ Admin: get all loans for their chama (with admin validation)
    public List<LoanApplicationDTO> getLoansByChama(Long chamaId) {
        logger.info("📋 Admin fetching all loans for chama {}", chamaId);

        List<LoanApplicationDTO> loans = loanRepo.findByMember_Chama_Id(chamaId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        logger.info("✅ Found {} loans for chama {}", loans.size(), chamaId);
        return loans;
    }

    // ✅ Validate if user is admin of a chama (helper method for controllers)
    public boolean isUserAdminOfChama(String username, Long chamaId) {
        try {
            UserEntity user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ChamaEntity chama = chamaRepo.findById(chamaId)
                    .orElseThrow(() -> new RuntimeException("Chama not found"));

            // Assuming your ChamaEntity has a createdBy field that stores user ID
            boolean isAdmin = chama.getCreatedBy().equals(user.getId());
            logger.info("🔍 User {} admin status for chama {}: {}", username, chamaId, isAdmin);
            return isAdmin;
        } catch (Exception e) {
            logger.error("❌ Error checking admin status: {}", e.getMessage());
            return false;
        }
    }

    // ✅ Update loan status (approve/reject) with chama validation
    public LoanApplicationDTO updateLoanStatus(Long id, String status, String adminUsername, Long chamaId) {
        logger.info("🔄 Admin {} updating loan {} status to {} in chama {}", adminUsername, id, status, chamaId);

        // Validate admin owns the chama
        if (!isUserAdminOfChama(adminUsername, chamaId)) {
            throw new RuntimeException("You are not authorized to modify loans in this chama");
        }

        LoanApplicationEntity loan = loanRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        // Double-check that loan belongs to the specified chama
        if (!loan.getMember().getChama().getId().equals(chamaId)) {
            throw new RuntimeException("Loan does not belong to the specified chama");
        }

        loan.setStatus(status.toUpperCase());
        loanRepo.save(loan);
        logger.info("✅ Loan {} status updated to {}", id, status);
        return mapToDTO(loan);
    }

    // ✅ Original update method (for backward compatibility)
    public LoanApplicationDTO updateLoanStatus(Long id, String status) {
        LoanApplicationEntity loan = loanRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setStatus(status.toUpperCase());
        loanRepo.save(loan);
        return mapToDTO(loan);
    }

    // ✅ Track loan repayment progress
    public LoanStatusDTO getLoanStatus(Long loanId) {
        LoanApplicationEntity loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        double totalPaid = paymentService.getTotalPaidForLoan(loanId);
        double balance = loan.getTotalRepayment() - totalPaid;

        return new LoanStatusDTO(loan, totalPaid, balance);
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

        if (entity.getMember() != null) {
            dto.memberId = entity.getMember().getId();
            dto.chamaId = entity.getMember().getChama().getId();
            dto.username = entity.getMember().getUser().getUsername();
        }

        double totalPaid = paymentService.getTotalPaidForLoan(entity.getId());
        double remaining = dto.totalRepayment != null ? dto.totalRepayment - totalPaid : 0.0;
        dto.remainingBalance = Math.max(remaining, 0.0);

        return dto;
    }
}