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

    @JsonProperty("tp")
    private String tp = "0"; // 거래유형 (0:전체, 1:입출금, 3:매매, 4:매수, 5:매도, 6:입금, 7:출금)

    @JsonProperty("gds_tp")
    private String gdsTp = "1"; // 상품구분 (0:전체, 1:국내주식, 2:수익증권, 3:해외주식, 4:금융상품)

    @JsonProperty("dmst_stex_tp")
    private String dmstStexTp = "KRX"; // 국내거래소구분 (KRX:한국거래소)

    @JsonProperty("crnc_cd")
    private String crncCd; // 통화코드 (공백:전체)

    @JsonProperty("frgn_stex_code")
    private String frgnStexCode; // 거래소코드 (%:전체)
}
