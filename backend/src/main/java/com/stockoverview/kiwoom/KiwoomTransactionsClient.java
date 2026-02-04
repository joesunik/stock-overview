package com.stockoverview.kiwoom;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockoverview.config.KiwoomProperties;
import com.stockoverview.kiwoom.dto.Kt00015Request;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KiwoomTransactionsClient {

    private static final String TR_PATH = "/api/dostk/acnt";
    private static final String API_ID = "kt00015";

    private final KiwoomProperties properties;
    private final RestTemplate restTemplate;
    private final KiwoomAuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * kt00015 위탁종합거래내역요청.
     * 공식 명세에 따라 응답 구조가 다를 수 있으므로, 리스트가 있으면 매핑하고 없으면 빈 목록 반환.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchTransactions(String acctNo, String strtDt, String endDt) {
        log.info("🔍 [kt00015] fetchTransactions 호출 - acctNo: '{}', strtDt: '{}', endDt: '{}'", acctNo, strtDt, endDt);

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
                // 국내주식 전용 엔드포인트이므로 gdsTp 미설정
                .build();

        log.info("📤 [kt00015] Request Body - acctNo: '{}', strtDt: '{}', endDt: '{}' (Builder 설정 후)",
                body.getAcctNo(), body.getStrtDt(), body.getEndDt());

        HttpEntity<Kt00015Request> entity = new HttpEntity<>(body, headers);
        String raw = restTemplate.postForObject(url, entity, String.class);
        log.info("📥 [kt00015] Response 수신 - 길이: {}", raw != null ? raw.length() : "null");
        log.info("📥 [kt00015] Response 전체: {}", raw);
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            Map<String, Object> map = objectMapper.readValue(raw, new TypeReference<>() {});
            Object code = map.get("return_code");
            if (code != null && !Integer.valueOf(0).equals(code)) {
                log.warn("❌ kt00015 error: return_code={}, return_msg={}", code, map.get("return_msg"));
                log.info("ℹ️ 요청 데이터: acctNo={}, strtDt={}, endDt={}, gdsTp={}", body.getAcctNo(), body.getStrtDt(), body.getEndDt(), body.getGdsTp());
                return List.of();
            }
            Object list = map.get("list");
            if (list instanceof List) {
                return (List<Map<String, Object>>) list;
            }
            Object items = map.get("items");
            if (items instanceof List) {
                return (List<Map<String, Object>>) items;
            }
            return List.of();
        } catch (Exception e) {
            log.warn("Failed to parse kt00015 response: {}", e.getMessage());
            return List.of();
        }
    }
}
