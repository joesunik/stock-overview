package com.stockoverview.kiwoom;

import com.stockoverview.config.KiwoomProperties;
import com.stockoverview.kiwoom.dto.Ka01690Request;
import com.stockoverview.kiwoom.dto.Ka01690Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class KiwoomDailyBalanceClient {

    private static final String TR_PATH = "/api/dostk/acnt";
    private static final String API_ID = "ka01690";

    private final KiwoomProperties properties;
    private final RestTemplate restTemplate;
    private final KiwoomAuthService authService;

    public Ka01690Response fetchDailyBalance(String acctNo, String qryDt) {
        String token = authService.getAccessToken();
        String url = properties.getBaseUrl() + TR_PATH;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        headers.set("api-id", API_ID);

        Ka01690Request body = Ka01690Request.builder()
                .acctNo(acctNo)
                .qryDt(qryDt)
                .build();

        log.info("🚀 [Kiwoom API] 요청 시작 - acctNo: {}, qryDt: {}", acctNo, qryDt);
        log.debug("📤 Request URL: {}", url);
        log.debug("📦 Request Body: acctNo={}, qryDt={}", body.getAcctNo(), body.getQryDt());

        HttpEntity<Ka01690Request> entity = new HttpEntity<>(body, headers);
        Ka01690Response response = restTemplate.postForObject(url, entity, Ka01690Response.class);
        if (response == null) {
            log.error("❌ [Kiwoom API] 응답 실패 - acctNo: {}, qryDt: {} (null response)", acctNo, qryDt);
            throw new KiwoomApiException("Empty response for date " + qryDt);
        }

        log.info("✅ [Kiwoom API] 응답 완료 - acctNo: {}, qryDt: {}, returnCode: {}", acctNo, qryDt, response.getReturnCode());
        log.debug("📥 Response Body - dayStkAsst: {}, dbstBal: {}, totEvltAmt: {}, totPrftRt: {}",
                response.getDayStkAsst(), response.getDbstBal(), response.getTotEvltAmt(), response.getTotPrftRt());

        return response;
    }
}
