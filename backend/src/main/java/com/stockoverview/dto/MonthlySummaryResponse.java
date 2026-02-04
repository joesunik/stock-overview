package com.stockoverview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MonthlySummaryResponse {

    @JsonProperty("start_date")
    private String startDate;
    @JsonProperty("end_date")
    private String endDate;
    @JsonProperty("start_asset")
    private BigDecimal startAsset;   // 기간 첫일 추정자산
    @JsonProperty("end_asset")
    private BigDecimal endAsset;     // 기간 말일 추정자산
    @JsonProperty("cumulative_return_pct")
    private String cumulativeReturnPct; // 기간 누적 수익률 %
    @JsonProperty("cumulative_change")
    private BigDecimal cumulativeChange; // 기간 누적 변동액
    @JsonProperty("monthly_summaries")
    private List<MonthlySummaryDto> monthlySummaries;
}
