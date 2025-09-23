package com.example.loanmanagement.Contribution;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamaRulesDTO {

    private Long id;

    @NotNull(message = "Chama ID is required")
    private Long chamaId;

    @NotNull(message = "Monthly contribution amount is required")
    @DecimalMin(value = "0.01", message = "Monthly contribution amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Monthly contribution amount must be a valid monetary value")
    private BigDecimal monthlyContributionAmount;

    @NotNull(message = "Penalty for late payment is required")
    @DecimalMin(value = "0.00", message = "Penalty amount cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Penalty amount must be a valid monetary value")
    private BigDecimal penaltyForLate;

    @NotNull(message = "Cycle type is required")
    private ChamaRulesEntity.CycleType cycleType;

    @NotNull(message = "Day of cycle is required")
    @Min(value = 1, message = "Day of cycle must be at least 1")
    @Max(value = 31, message = "Day of cycle cannot exceed 31")
    private Integer dayOfCycle;

    @NotNull(message = "Grace period days is required")
    @Min(value = 0, message = "Grace period days cannot be negative")
    @Max(value = 30, message = "Grace period cannot exceed 30 days")
    private Integer gracePeriodDays;

    private String payoutOrder;

    private Long currentPayoutMemberId;

    // Additional fields for response
    private String chamaName;

    // Constructor for creating from entity
    public ChamaRulesDTO(ChamaRulesEntity entity) {
        this.id = entity.getId();
        this.monthlyContributionAmount = entity.getMonthlyContributionAmount();
        this.penaltyForLate = entity.getPenaltyForLate();
        this.cycleType = entity.getCycleType();
        this.dayOfCycle = entity.getDayOfCycle();
        this.gracePeriodDays = entity.getGracePeriodDays();
        this.payoutOrder = entity.getPayoutOrder();
        this.currentPayoutMemberId = entity.getCurrentPayoutMemberId();

        if (entity.getChama() != null) {
            this.chamaId = entity.getChama().getId();
            this.chamaName = entity.getChama().getName();
        }
    }
}