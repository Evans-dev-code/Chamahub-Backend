package com.example.loanmanagement.Loanapplication;

import com.example.loanmanagement.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplicationEntity, Long> {
    // Find loans by member (since user can belong to multiple chamas)
    List<LoanApplicationEntity> findByMember(MemberEntity member);

    // Find all loans for a chama
    List<LoanApplicationEntity> findByMember_Chama_Id(Long chamaId);
}
