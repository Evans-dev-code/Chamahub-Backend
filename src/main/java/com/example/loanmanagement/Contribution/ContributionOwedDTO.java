package com.example.loanmanagement.Contribution;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// DTO for contribution owed calculations
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContributionOwedDTO {
    private Long memberId;
    private Long chamaId;
    private String currentCycle;
    private BigDecimal expectedAmount;
    private BigDecimal amountOwed;
    private BigDecimal penaltyAmount;
    private String status; // PAID, PENDING, OVERDUE
    private LocalDate dueDate;
    private LocalDate lastPaymentDate;
    private String memberName;
    private String chamaName;
}

