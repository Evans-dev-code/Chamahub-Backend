package com.example.loanmanagement.Contribution;

import com.example.loanmanagement.Member.MemberEntity;
import com.example.loanmanagement.Chama.ChamaEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contributions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContributionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate datePaid;

    @Column(nullable = false, length = 50)
    private String cycle; // e.g., "January 2025", "Week 1 2025"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContributionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chama_id", nullable = false)
    private ChamaEntity chama;

    @Column(nullable = true, precision = 19, scale = 2)
    private BigDecimal penaltyAmount; // Late penalty applied

    @Column(nullable = true)
    private String notes; // Optional notes for the contribution

    public enum ContributionStatus {
        ON_TIME,
        LATE,
        PENDING
    }
}