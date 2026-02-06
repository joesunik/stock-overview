package com.stockoverview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DailyBalanceDto {

    @JsonProperty("date")
    private String date;           // yyyy-MM-dd
    @JsonProperty("estimated_asset")
    private BigDecimal estimatedAsset;  // 추정자산
    @JsonProperty("deposit_balance")
    private BigDecimal depositBalance;  // 예수금
    @JsonProperty("total_evlt_amt")
    private BigDecimal totalEvltAmt;     // 총 평가금액
    @JsonProperty("profit_rate")
    private String profitRate;     // 수익률
}
