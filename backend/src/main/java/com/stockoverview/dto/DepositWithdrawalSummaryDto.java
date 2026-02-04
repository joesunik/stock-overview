package com.stockoverview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DepositWithdrawalSummaryDto {

    @JsonProperty("total_deposit")
    private BigDecimal totalDeposit;

    @JsonProperty("total_withdrawal")
    private BigDecimal totalWithdrawal;
}
