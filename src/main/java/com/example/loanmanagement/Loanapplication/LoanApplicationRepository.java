package com.example.loanmanagement.Loanapplication;

import com.example.loanmanagement.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplicationEntity, Long> {
    List<LoanApplicationEntity> findByUser(UserEntity user);
}
