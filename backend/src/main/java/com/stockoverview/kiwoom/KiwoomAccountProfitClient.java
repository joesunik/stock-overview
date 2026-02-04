package com.stockoverview.kiwoom;

import com.stockoverview.config.KiwoomProperties;
import com.stockoverview.kiwoom.dto.Kt00016Request;
import com.stockoverview.kiwoom.dto.Kt00016Response;
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
public class KiwoomAccountProfitClient {

    private static final String TR_PATH = "/api/rlsn/daily/acnt";
    private static final String API_ID = "kt00016";

    private final KiwoomProperties properties;
    private final RestTemplate restTemplate;
    private final KiwoomAuthService authService;

    public Kt00016Response fetchAccountProfit(String acctNo, String frDt, String toDt) {
        // 필수 파라미터 검증
        if (acctNo == null || acctNo.isBlank()) {
            log.error("❌ [kt00016] 계좌번호 누락");
            throw new IllegalArgumentException("계좌번호는 필수입니다");
        }
        if (frDt == null || frDt.isBlank()) {
            log.error("❌ [kt00016] 시작일 누락");
            throw new IllegalArgumentException("시작일(fr_dt)은 필수입니다");
        }
        if (toDt == null || toDt.isBlank()) {
            log.error("❌ [kt00016] 종료일 누락");
            throw new IllegalArgumentException("종료일(to_dt)은 필수입니다");
        }

        String token = authService.getAccessToken();
        String url = properties.getBaseUrl() + TR_PATH;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        headers.set("api-id", API_ID);

        Kt00016Request body = Kt00016Request.builder()
                .acctNo(acctNo)
                .frDt(frDt)
                .toDt(toDt)
                .build();

        log.info("📈 [Kiwoom API] 계좌수익률 조회 시작 - acctNo: {}, frDt: {}, toDt: {}", acctNo, frDt, toDt);
        log.debug("📤 Request URL: {}", url);
        log.debug("📦 Request Body: acctNo={}, frDt={}, toDt={}", body.getAcctNo(), body.getFrDt(), body.getToDt());

        HttpEntity<Kt00016Request> entity = new HttpEntity<>(body, headers);

        try {
            Kt00016Response response = restTemplate.postForObject(url, entity, Kt00016Response.class);

            if (response == null) {
                log.error("❌ [Kiwoom API] 계좌수익률 응답 실패 - acctNo: {}, frDt: {}, toDt: {} (null response)", acctNo, frDt, toDt);
                throw new KiwoomApiException("Empty response for account profit data - " + frDt);
            }

            log.info("✅ [Kiwoom API] 계좌수익률 조회 완료 - acctNo: {}, frDt: {}, toDt: {}, returnCode: {}, prftRt: {}",
                    acctNo, frDt, toDt, response.getReturnCode(), response.getPrftRt());
            log.debug("📥 Response: futrReplSella={}, evltvPrft={}, totAmtFr={}, totAmtTo={}",
                    response.getFutrReplSella(), response.getEvltvPrft(), response.getTotAmtFr(), response.getTotAmtTo());

            return response;
        } catch (HttpServerErrorException e) {
            log.error("❌ [Kiwoom API] kt00016 서버 에러 - acctNo: {}, frDt: {}, toDt: {}, status: {}, body: {}",
                    acctNo, frDt, toDt, e.getStatusCode(), e.getResponseBodyAsString());
            throw new KiwoomApiException("kt00016 API error: " + e.getMessage());
        }
    }
}
