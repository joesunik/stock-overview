package com.stockoverview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransactionDto {

    @JsonProperty("date")
    private String date;       // yyyy-MM-dd

    @JsonProperty("type")
    private String type;       // 구분: 입금, 출금, 매수, 매도 등

    @JsonProperty("amount")
    private BigDecimal amount; // 금액

    @JsonProperty("remark")
    private String remark;     // 비고 (당시 환율, 원화환산 등)
}
