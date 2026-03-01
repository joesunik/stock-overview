package com.sso.web;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sso.service.TokenExchangeService;

/** BFF용 토큰 API: 인증 코드↔토큰 교환, 리프레시 토큰으로 갱신. */
@RestController
@RequestMapping("/api/auth")
public class TokenController {

    private static final String DEFAULT_CLIENT_ID = "sso-client";
    private final TokenExchangeService tokenExchangeService;

    public TokenController(TokenExchangeService tokenExchangeService) {
        this.tokenExchangeService = tokenExchangeService;
    }

    /** 인증 코드를 액세스/리프레시 토큰으로 교환. */
    @PostMapping(value = "/exchange", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> exchange(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String redirectUri = body.get("redirectUri");
        String clientId = body.getOrDefault("client_id", DEFAULT_CLIENT_ID);
        return tokenExchangeService.exchangeCode(clientId, code, redirectUri);
    }

    /** 리프레시 토큰으로 새 액세스(및 리프레시) 토큰 발급. */
    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refresh_token");
        String clientId = body.getOrDefault("client_id", DEFAULT_CLIENT_ID);
        return tokenExchangeService.refreshToken(clientId, refreshToken);
    }
}
