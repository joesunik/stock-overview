package com.sso.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** OAuth2 클라이언트 엔티티. clientId/비밀/redirect URIs/스코프 등 DB 저장. */
@Entity
@Table(name = "oauth2_client")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String clientId;

    @Column(nullable = false, length = 500)
    private String clientSecretEncoded;

    /** Comma-separated redirect URIs */
    @Column(nullable = false, length = 2000)
    private String redirectUris;

    /** Comma-separated post-logout redirect URIs */
    @Column(length = 2000)
    private String postLogoutRedirectUris;

    /** Comma-separated authorization grant types, e.g. authorization_code,refresh_token */
    @Column(nullable = false, length = 500)
    private String authorizationGrantTypes = "authorization_code,refresh_token";

    /** Comma-separated scopes, e.g. openid,profile */
    @Column(nullable = false, length = 500)
    private String scopes = "openid,profile";

    @Column(nullable = false)
    private boolean requireAuthorizationConsent = true;

    public OAuth2Client(String clientId, String clientSecretEncoded, String redirectUris,
            String postLogoutRedirectUris, String authorizationGrantTypes, String scopes,
            boolean requireAuthorizationConsent) {
        this.clientId = clientId;
        this.clientSecretEncoded = clientSecretEncoded;
        this.redirectUris = redirectUris;
        this.postLogoutRedirectUris = postLogoutRedirectUris;
        this.authorizationGrantTypes = authorizationGrantTypes != null ? authorizationGrantTypes : "authorization_code,refresh_token";
        this.scopes = scopes != null ? scopes : "openid,profile";
        this.requireAuthorizationConsent = requireAuthorizationConsent;
    }
}
