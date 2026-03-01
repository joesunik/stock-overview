package com.sso.web.dto;

import java.util.List;

/** 관리자 API용 OAuth2 클라이언트 응답 DTO. redirect URIs는 리스트로. */
public record OAuth2ClientDto(
        Long id,
        String clientId,
        List<String> redirectUris,
        List<String> postLogoutRedirectUris,
        boolean requireAuthorizationConsent
) {
}
