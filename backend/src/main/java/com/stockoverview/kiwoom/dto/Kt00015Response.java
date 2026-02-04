package com.stockoverview.kiwoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kt00015Response {

    @JsonProperty("return_code")
    private String returnCode; // 정상 여부 확인

    @JsonProperty("return_message")
    private String returnMessage;

    @JsonProperty("trst_ovrl_trde_prps_array")
    private List<ForeignStockItem> trstOvrlTrdePrpsArray; // 거래내역 배열

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForeignStockItem {
        @JsonProperty("trde_dt")
        private String trdeDt; // 거래일자

        @JsonProperty("trde_amt")
        private String trdeAmt; // 거래금액 (원화)

        @JsonProperty("fc_trde_amt")
        private String fcTrdeAmt; // 거래금액 (외화)

        @JsonProperty("exct_amt")
        private String exctAmt; // 정산금액 (원화)

        @JsonProperty("fc_exct_amt")
        private String fcExctAmt; // 정산금액 (외화)

        @JsonProperty("fc_entra")
        private String fcEntra; // 외화예수금잔고

        @JsonProperty("entr_remn")
        private String entrRemn; // 예수금잔고 (원화)

        @JsonProperty("crnc_cd")
        private String crncCd; // 통화코드 (KRW, USD 등)

        @JsonProperty("trde_unit")
        private String trdeUnit; // 거래단가/환율

        @JsonProperty("stk_cd")
        private String stkCd; // 종목코드

        @JsonProperty("stk_nm")
        private String stkNm; // 종목명

        @JsonProperty("trde_qty_jwa_cnt")
        private String trdeQtyJwaCnt; // 거래수량/좌수
    }
}
