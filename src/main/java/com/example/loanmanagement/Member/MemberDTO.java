package com.example.loanmanagement.Member;

import com.example.loanmanagement.Enum.ChamaRole;
import java.time.LocalDate;

public class MemberDTO {

    private String phoneNumber;
    private ChamaRole chamaRole;   // use Enum instead of String
    private Long userId;
    private Long chamaId;
    private String joinCode;       // ✅ Added field
    private LocalDate joinedDate;

    public MemberDTO() {}

    public MemberDTO(String phoneNumber, ChamaRole chamaRole, Long userId, Long chamaId, String joinCode) {
        this.phoneNumber = phoneNumber;
        this.chamaRole = chamaRole;
        this.userId = userId;
        this.chamaId = chamaId;
        this.joinCode = joinCode;
        this.joinedDate = LocalDate.now();
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public ChamaRole getChamaRole() { return chamaRole; }
    public void setChamaRole(ChamaRole chamaRole) { this.chamaRole = chamaRole; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getChamaId() { return chamaId; }
    public void setChamaId(Long chamaId) { this.chamaId = chamaId; }

    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

    public LocalDate getJoinedDate() { return joinedDate; }
    public void setJoinedDate(LocalDate joinedDate) { this.joinedDate = joinedDate; }
}
