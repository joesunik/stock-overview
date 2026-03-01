package com.sso.config.security;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Component;

import com.sso.domain.OAuth2Client;
import com.sso.repository.OAuth2ClientRepository;

/** OAuth2 인가 서버가 DB의 OAuth2Client를 RegisteredClient로 조회하기 위한 어댑터. */
@Component
public class JpaRegisteredClientRepository implements RegisteredClientRepository {

    private final OAuth2ClientRepository oauth2ClientRepository;

    public JpaRegisteredClientRepository(OAuth2ClientRepository oauth2ClientRepository) {
        this.oauth2ClientRepository = oauth2ClientRepository;
    }

    @Override
    /** 엔티티 없으면 새로 생성, 있으면 redirect/scopes 등만 갱신 후 저장. */
    public void save(RegisteredClient registeredClient) {
        OAuth2Client entity = oauth2ClientRepository.findByClientId(registeredClient.getClientId())
                .orElse(null);
        if (entity == null) {
            entity = new OAuth2Client(
                    registeredClient.getClientId(),
                    registeredClient.getClientSecret() != null ? registeredClient.getClientSecret() : "",
                    String.join(",", registeredClient.getRedirectUris()),
                    registeredClient.getPostLogoutRedirectUris() != null && !registeredClient.getPostLogoutRedirectUris().isEmpty()
                            ? String.join(",", registeredClient.getPostLogoutRedirectUris()) : null,
                    registeredClient.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::getValue).collect(Collectors.joining(",")),
                    String.join(",", registeredClient.getScopes()),
                    registeredClient.getClientSettings().isRequireAuthorizationConsent()
            );
        } else {
            entity.setClientSecretEncoded(registeredClient.getClientSecret() != null ? registeredClient.getClientSecret() : "");
            entity.setRedirectUris(String.join(",", registeredClient.getRedirectUris()));
            entity.setPostLogoutRedirectUris(registeredClient.getPostLogoutRedirectUris() != null && !registeredClient.getPostLogoutRedirectUris().isEmpty()
                    ? String.join(",", registeredClient.getPostLogoutRedirectUris()) : null);
            entity.setAuthorizationGrantTypes(registeredClient.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::getValue).collect(Collectors.joining(",")));
            entity.setScopes(String.join(",", registeredClient.getScopes()));
            entity.setRequireAuthorizationConsent(registeredClient.getClientSettings().isRequireAuthorizationConsent());
        }
        oauth2ClientRepository.save(entity);
    }

    @Override
    public RegisteredClient findById(String id) {
        return oauth2ClientRepository.findById(Long.parseLong(id))
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return oauth2ClientRepository.findByClientId(clientId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    /** DB 엔티티를 Spring OAuth2 RegisteredClient로 변환(쉼표 구분 문자열 → Set). */
    private RegisteredClient toRegisteredClient(OAuth2Client e) {
        Set<String> redirectUriSet = split(e.getRedirectUris());
        Set<String> postLogoutSet = e.getPostLogoutRedirectUris() != null
                ? split(e.getPostLogoutRedirectUris()) : Set.of();
        Set<AuthorizationGrantType> grantTypes = split(e.getAuthorizationGrantTypes()).stream()
                .map(AuthorizationGrantType::new)
                .collect(Collectors.toSet());
        Set<String> scopeSet = split(e.getScopes());

        return RegisteredClient.withId(String.valueOf(e.getId()))
                .clientId(e.getClientId())
                .clientSecret(e.getClientSecretEncoded())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantTypes(g -> g.addAll(grantTypes))
                .redirectUris(u -> u.addAll(redirectUriSet))
                .postLogoutRedirectUris(u -> u.addAll(postLogoutSet))
                .scopes(s -> s.addAll(scopeSet))
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(e.isRequireAuthorizationConsent())
                        .build())
                .build();
    }

    private static Set<String> split(String s) {
        if (s == null || s.isBlank()) return Set.of();
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toSet());
    }
}
