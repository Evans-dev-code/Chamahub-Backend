package com.example.loanmanagement.Contribution;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// DTO for merry-go-round payout calculations
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberPayoutDTO {
    private Long chamaId;
    private String cycle;
    private Long nextPayoutMemberId;
    private String nextPayoutMemberName;
    private BigDecimal payoutAmount;
    private LocalDate payoutDate;
    private Integer totalMembers;
    private Integer membersContributed;
    private Boolean allMembersPaid;
}
