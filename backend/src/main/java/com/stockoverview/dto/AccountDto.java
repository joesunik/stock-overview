package com.stockoverview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDto {

    private Long id;
    @JsonProperty("acct_no")
    private String acctNo;
    @JsonProperty("is_default")
    private Boolean isDefault;
}
