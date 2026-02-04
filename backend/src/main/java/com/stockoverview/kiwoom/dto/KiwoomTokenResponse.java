package com.stockoverview.kiwoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KiwoomTokenResponse {

    @JsonProperty("expires_dt")
    private String expiresDt;

    @JsonProperty("token_type")
    private String tokenType;

    private String token;

    @JsonProperty("return_code")
    private Integer returnCode;

    @JsonProperty("return_msg")
    private String returnMsg;
}
