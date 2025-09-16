package com.example.loanmanagement.Loanpayment;

import com.example.loanmanagement.Loanapplication.LoanApplicationEntity;
import com.example.loanmanagement.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanpaymentRepository extends JpaRepository<LoanpaymentEntity, Long> {
    List<LoanpaymentEntity> findByLoan(LoanApplicationEntity loan);
    List<LoanpaymentEntity> findByPaidBy(UserEntity user);

    // ✅ New chama-aware query
    List<LoanpaymentEntity> findByLoan_Member_Chama_Id(Long chamaId);
}
