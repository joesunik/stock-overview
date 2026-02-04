package com.stockoverview.kiwoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Kt00016Request {

    @JsonProperty("acct_no")
    private String acctNo; // 계좌번호

    @JsonProperty("fr_dt")
    private String frDt; // 시작일자 (YYYYMMDD)

    @JsonProperty("to_dt")
    private String toDt; // 종료일자 (YYYYMMDD)
}
