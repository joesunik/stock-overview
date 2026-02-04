package com.stockoverview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MonthlySummaryDto {

    private String yearMonth;      // yyyy-MM
    @JsonProperty("start_asset")
    private BigDecimal startAsset;   // 해당 월 첫 영업일 추정자산
    @JsonProperty("end_asset")
    private BigDecimal endAsset;     // 해당 월 마지막 영업일 추정자산
    @JsonProperty("monthly_change")
    private BigDecimal monthlyChange; // 월간 변동액
    @JsonProperty("monthly_return_pct")
    private String monthlyReturnPct;  // 월간 수익률 %
}
