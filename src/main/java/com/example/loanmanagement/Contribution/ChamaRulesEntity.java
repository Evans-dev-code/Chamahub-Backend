package com.example.loanmanagement.Contribution;

import com.example.loanmanagement.Chama.ChamaEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "chama_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamaRulesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chama_id", nullable = false, unique = true)
    private ChamaEntity chama;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyContributionAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal penaltyForLate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CycleType cycleType;

    @Column(nullable = false)
    private Integer dayOfCycle; // Day of month (1-31) for monthly, day of week (1-7) for weekly

    @Column(nullable = false)
    private Integer gracePeriodDays; // Days after due date before penalty applies

    @Column(nullable = true)
    private String payoutOrder; // JSON string storing member order for merry-go-round

    @Column(nullable = true)
    private Long currentPayoutMemberId; // Current member to receive payout

    public enum CycleType {
        WEEKLY,
        MONTHLY
    }
}