package com.stockoverview.kiwoom;

import com.stockoverview.config.KiwoomProperties;
import com.stockoverview.kiwoom.dto.Kt00015Request;
import com.stockoverview.kiwoom.dto.Kt00015Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException;

@Component
@RequiredArgsConstructor
@Slf4j
public class KiwoomForeignStockClient {

    private static final String TR_PATH = "/api/trst/ovrl/trde";
    private static final String API_ID = "kt00015";

    private final KiwoomProperties properties;
    private final RestTemplate restTemplate;
    private final KiwoomAuthService authService;

    public Kt00015Response fetchForeignStockHistory(String acctNo, String strtDt, String endDt) {
        // 필수 파라미터 검증
        if (acctNo == null || acctNo.isBlank()) {
            log.error("❌ [kt00015] 계좌번호 누락");
            throw new IllegalArgumentException("계좌번호는 필수입니다");
        }
        if (strtDt == null || strtDt.isBlank()) {
            log.error("❌ [kt00015] 시작일 누락");
            throw new IllegalArgumentException("시작일(strt_dt)은 필수입니다");
        }
        if (endDt == null || endDt.isBlank()) {
            log.error("❌ [kt00015] 종료일 누락");
            throw new IllegalArgumentException("종료일(end_dt)은 필수입니다");
        }

        String token = authService.getAccessToken();
        String url = properties.getBaseUrl() + TR_PATH;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        headers.set("api-id", API_ID);

        Kt00015Request body = Kt00015Request.builder()
                .acctNo(acctNo)
                .strtDt(strtDt)
                .endDt(endDt)
                .gdsTp("3") // 해외주식
                .crncCd("") // 통화코드 (공백:전체)
                .frgnStexCode("%") // 거래소코드 (전체)
                .build();

        log.info("🌍 [Kiwoom API] 해외주식 조회 시작 - acctNo: {}, strtDt: {}, endDt: {}", acctNo, strtDt, endDt);
        log.debug("📤 Request URL: {}", url);
        log.debug("📦 Request Body: acctNo={}, strtDt={}, endDt={}, gdsTp={}", body.getAcctNo(), body.getStrtDt(), body.getEndDt(), body.getGdsTp());

        HttpEntity<Kt00015Request> entity = new HttpEntity<>(body, headers);

        try {
            Kt00015Response response = restTemplate.postForObject(url, entity, Kt00015Response.class);

            if (response == null) {
                log.error("❌ [Kiwoom API] 해외주식 응답 실패 - acctNo: {}, strtDt: {}, endDt: {} (null response)", acctNo, strtDt, endDt);
                throw new KiwoomApiException("Empty response for foreign stock data - " + strtDt);
            }

            log.info("✅ [Kiwoom API] 해외주식 조회 완료 - acctNo: {}, strtDt: {}, endDt: {}, returnCode: {}", acctNo, strtDt, endDt, response.getReturnCode());
            if (response.getTrstOvrlTrdePrpsArray() != null) {
                log.debug("📥 거래내역 건수: {}", response.getTrstOvrlTrdePrpsArray().size());
            }

            return response;
        } catch (HttpServerErrorException e) {
            log.error("❌ [Kiwoom API] kt00015 서버 에러 - acctNo: {}, strtDt: {}, endDt: {}, status: {}, body: {}",
                    acctNo, strtDt, endDt, e.getStatusCode(), e.getResponseBodyAsString());
            throw new KiwoomApiException("kt00015 API error: " + e.getMessage());
        }
    }
}
