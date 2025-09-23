package com.example.loanmanagement.Contribution;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContributionDTO {

    private Long id;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Amount must be a valid monetary value")
    private BigDecimal amount;

    @NotNull(message = "Date paid is required")
    private LocalDate datePaid;

    @NotBlank(message = "Cycle is required")
    @Size(max = 50, message = "Cycle must not exceed 50 characters")
    private String cycle;

    private ContributionEntity.ContributionStatus status;

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Chama ID is required")
    private Long chamaId;

    private BigDecimal penaltyAmount;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;

    // Additional fields for response DTOs
    private String memberName;
    private String chamaName;
    private BigDecimal expectedAmount;
    private Boolean isLate;

    // Constructor for creating from entity
    public ContributionDTO(ContributionEntity entity) {
        this.id = entity.getId();
        this.amount = entity.getAmount();
        this.datePaid = entity.getDatePaid();
        this.cycle = entity.getCycle();
        this.status = entity.getStatus();
        this.penaltyAmount = entity.getPenaltyAmount();
        this.notes = entity.getNotes();

        if (entity.getMember() != null) {
            this.memberId = entity.getMember().getId();
            this.memberName = entity.getMember().getUser().getUsername();
        }

        if (entity.getChama() != null) {
            this.chamaId = entity.getChama().getId();
            this.chamaName = entity.getChama().getName();
        }
    }
}