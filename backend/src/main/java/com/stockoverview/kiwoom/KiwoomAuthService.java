package com.stockoverview.kiwoom;

import com.stockoverview.config.KiwoomProperties;
import com.stockoverview.kiwoom.dto.KiwoomTokenRequest;
import com.stockoverview.kiwoom.dto.KiwoomTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class KiwoomAuthService {

    private static final String TOKEN_PATH = "/oauth2/token";

    private final KiwoomProperties properties;
    private final RestTemplate restTemplate;

    private volatile String cachedToken;
    private volatile long cachedTokenExpiresAt;

    public String getAccessToken() {
        if (cachedToken != null && System.currentTimeMillis() < cachedTokenExpiresAt - 60_000) {
            return cachedToken;
        }
        synchronized (this) {
            if (cachedToken != null && System.currentTimeMillis() < cachedTokenExpiresAt - 60_000) {
                return cachedToken;
            }
            KiwoomTokenRequest request = KiwoomTokenRequest.builder()
                    .grantType("client_credentials")
                    .appkey(properties.getAppKey())
                    .secretkey(properties.getSecretKey())
                    .build();
            String url = properties.getBaseUrl() + TOKEN_PATH;
            KiwoomTokenResponse response = restTemplate.postForObject(url, request, KiwoomTokenResponse.class);
            if (response == null || response.getReturnCode() == null || response.getReturnCode() != 0) {
                throw new KiwoomApiException("Failed to get token: " + (response != null ? response.getReturnMsg() : "null response"));
            }
            cachedToken = response.getToken();
            cachedTokenExpiresAt = parseExpiresDt(response.getExpiresDt());
            log.debug("Kiwoom token acquired, expires at {}", response.getExpiresDt());
            return cachedToken;
        }
    }

    private static long parseExpiresDt(String expiresDt) {
        if (expiresDt == null || expiresDt.length() < 14) {
            return System.currentTimeMillis() + 24 * 60 * 60 * 1000;
        }
        try {
            int year = Integer.parseInt(expiresDt.substring(0, 4));
            int month = Integer.parseInt(expiresDt.substring(4, 6));
            int day = Integer.parseInt(expiresDt.substring(6, 8));
            int hour = Integer.parseInt(expiresDt.substring(8, 10));
            int min = Integer.parseInt(expiresDt.substring(10, 12));
            int sec = Integer.parseInt(expiresDt.substring(12, 14));
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month - 1, day, hour, min, sec);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        } catch (Exception e) {
            return System.currentTimeMillis() + 24 * 60 * 60 * 1000;
        }
    }
}
