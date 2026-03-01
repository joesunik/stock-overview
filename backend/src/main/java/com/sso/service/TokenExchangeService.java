package com.sso.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/** BFF에서 받은 인증 코드/리프레시 토큰을 내부 /oauth2/token 호출로 실제 토큰 발급. */
@Service
public class TokenExchangeService {

    private static final String SSO_CLIENT_ID = "sso-client";

    private final RegisteredClientRepository registeredClientRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String tokenUri;
    private final String ssoClientSecret;

    public TokenExchangeService(
            RegisteredClientRepository registeredClientRepository,
            @Value("${server.port:8080}") int port,
            @Value("${app.oauth2.sso-client-secret:secret}") String ssoClientSecret) {
        this.registeredClientRepository = registeredClientRepository;
        this.tokenUri = "http://localhost:" + port + "/oauth2/token";
        this.ssoClientSecret = ssoClientSecret;
    }

    /** 인증 코드로 액세스/리프레시 토큰 발급. */
    public Map<String, Object> exchangeCode(String clientId, String code, String redirectUri) {
        if (code == null || redirectUri == null) {
            throw new IllegalArgumentException("code and redirectUri required");
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(OAuth2ParameterNames.CODE, code);
        form.add(OAuth2ParameterNames.REDIRECT_URI, redirectUri);
        form.add(OAuth2ParameterNames.GRANT_TYPE, "authorization_code");
        return tokenRequest(clientId, form);
    }

    /** 리프레시 토큰으로 새 액세스(및 리프레시) 토큰 발급. */
    public Map<String, Object> refreshToken(String clientId, String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("refresh_token required");
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(OAuth2ParameterNames.GRANT_TYPE, "refresh_token");
        form.add(OAuth2ParameterNames.REFRESH_TOKEN, refreshToken);
        return tokenRequest(clientId, form);
    }

    /** DB에서 클라이언트 조회 후 /oauth2/token에 폼 전송. sso-client는 설정의 평문 시크릿 사용(DB는 BCrypt). */
    @SuppressWarnings("unchecked")
    private Map<String, Object> tokenRequest(String clientId, MultiValueMap<String, String> form) {
        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        if (client == null || client.getClientSecret() == null) {
            throw new IllegalArgumentException("Unknown client or missing secret: " + clientId);
        }
        String plainSecret = SSO_CLIENT_ID.equals(clientId)
                ? ssoClientSecret
                : stripNoopPrefix(client.getClientSecret());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, plainSecret);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
        return restTemplate.postForObject(tokenUri, entity, Map.class);
    }

    private static String stripNoopPrefix(String encoded) {
        if (encoded == null) return null;
        if (encoded.startsWith("{") && encoded.contains("}")) {
            return encoded.substring(encoded.indexOf('}') + 1);
        }
        return encoded;
    }
}
