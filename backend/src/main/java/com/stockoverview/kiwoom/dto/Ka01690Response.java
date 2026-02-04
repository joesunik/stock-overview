package com.stockoverview.kiwoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Ka01690Response {

    @JsonProperty("return_code")
    private Integer returnCode;
    @JsonProperty("return_msg")
    private String returnMsg;

    private String dt;              // 일자
    @JsonProperty("tot_buy_amt")
    private String totBuyAmt;      // 총 매입가
    @JsonProperty("tot_evlt_amt")
    private String totEvltAmt;     // 총 평가금액
    @JsonProperty("tot_evltv_prft")
    private String totEvltvPrft;   // 총 평가손익
    @JsonProperty("tot_prft_rt")
    private String totPrftRt;      // 수익률
    @JsonProperty("dbst_bal")
    private String dbstBal;        // 예수금
    @JsonProperty("day_stk_asst")
    private String dayStkAsst;     // 추정자산
    @JsonProperty("buy_wght")
    private String buyWght;        // 현금비중
    @JsonProperty("day_bal_rt")
    private List<DayBalRtItem> dayBalRt;

    @Data
    public static class DayBalRtItem {
        @JsonProperty("cur_prc")
        private String curPrc;
        @JsonProperty("stk_cd")
        private String stkCd;
        @JsonProperty("stk_nm")
        private String stkNm;
        @JsonProperty("rmnd_qty")
        private String rmndQty;
        @JsonProperty("buy_uv")
        private String buyUv;
        @JsonProperty("buy_wght")
        private String buyWght;
        @JsonProperty("evltv_prft")
        private String evltvPrft;
        @JsonProperty("prft_rt")
        private String prftRt;
        @JsonProperty("evlt_amt")
        private String evltAmt;
        @JsonProperty("evlt_wght")
        private String evltWght;
    }
}
