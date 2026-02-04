package com.stockoverview.kiwoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kt00016Response {

    @JsonProperty("return_code")
    private String returnCode; // 정상 여부 확인

    @JsonProperty("return_message")
    private String returnMessage;

    @JsonProperty("futr_repl_sella")
    private String futrReplSella; // 선물대용매도금액

    @JsonProperty("evltv_prft")
    private String evltvPrft; // 평가손익

    @JsonProperty("prft_rt")
    private String prftRt; // 수익률

    @JsonProperty("tot_amt_fr")
    private String totAmtFr; // 순자산액계_초

    @JsonProperty("tot_amt_to")
    private String totAmtTo; // 순자산액계_말
}
