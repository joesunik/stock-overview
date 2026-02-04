package com.stockoverview.kiwoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Ka01690Request {

    @JsonProperty("acct_no")
    private String acctNo; // 계좌번호

    @JsonProperty("qry_dt")
    private String qryDt; // YYYYMMDD
}
