package com.example.loanmanagement.Contribution;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// DTO for contribution summary/statistics
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContributionSummaryDTO {
    private Long chamaId;
    private String cycle;
    private Integer totalMembers;
    private Integer membersContributed;
    private Integer pendingContributions;
    private Integer lateContributions;
    private BigDecimal totalCollected;
    private BigDecimal expectedTotal;
    private BigDecimal collectionRate; // Percentage
    private String chamaName;}
