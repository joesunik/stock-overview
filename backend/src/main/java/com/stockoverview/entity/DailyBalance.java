package com.stockoverview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "daily_balance", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "account_id", "balance_date" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "balance_date", nullable = false)
    private LocalDate balanceDate;

    @Column(name = "estimated_asset", precision = 20, scale = 2)
    private BigDecimal estimatedAsset;

    @Column(name = "deposit_balance", precision = 20, scale = 2)
    private BigDecimal depositBalance;

    @Column(name = "total_evlt_amt", precision = 20, scale = 2)
    private BigDecimal totalEvltAmt;

    @Column(name = "profit_rate", length = 20)
    private String profitRate;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
