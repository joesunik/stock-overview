package com.stockoverview.kiwoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Kt00015Request {

    @JsonProperty("acct_no")
    private String acctNo; // 계좌번호

    @JsonProperty("strt_dt")
    private String strtDt; // 시작일자 (YYYYMMDD)

    @JsonProperty("end_dt")
    private String endDt; // 종료일자 (YYYYMMDD)

    @JsonProperty("gds_tp")
    private String gdsTp; // 상품구분 (3:해외주식)

    @JsonProperty("crnc_cd")
    private String crncCd; // 통화코드 (공백:전체)

    @JsonProperty("frgn_stex_code")
    private String frgnStexCode; // 거래소코드 (%:전체)
}
