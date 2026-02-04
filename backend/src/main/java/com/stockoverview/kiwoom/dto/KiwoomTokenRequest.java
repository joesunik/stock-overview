package com.stockoverview.kiwoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KiwoomTokenRequest {

    @JsonProperty("grant_type")
    private String grantType;

    private String appkey;

    private String secretkey;
}
