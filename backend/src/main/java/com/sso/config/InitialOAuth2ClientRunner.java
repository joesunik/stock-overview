package com.sso.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sso.domain.OAuth2Client;
import com.sso.repository.OAuth2ClientRepository;

/** 기동 시 OAuth2 클라이언트 sso-client가 없으면 redirect 3000/5173 등으로 한 건 생성. 시크릿은 BCrypt로 저장(인가 서버 검증용). */
@Component
public class InitialOAuth2ClientRunner implements ApplicationRunner {

    private final OAuth2ClientRepository oauth2ClientRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialOAuth2ClientRunner(OAuth2ClientRepository oauth2ClientRepository,
            PasswordEncoder passwordEncoder) {
        this.oauth2ClientRepository = oauth2ClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        oauth2ClientRepository.findByClientId("sso-client").ifPresentOrElse(
                (existing) -> {
                    // 기존에 {noop}secret으로 저장된 경우 BCrypt로 갱신
                    String enc = existing.getClientSecretEncoded();
                    if (enc != null && enc.startsWith("{noop}")) {
                        existing.setClientSecretEncoded(passwordEncoder.encode("secret"));
                        oauth2ClientRepository.save(existing);
                    }
                },
                () -> {
                    OAuth2Client client = new OAuth2Client(
                            "sso-client",
                            passwordEncoder.encode("secret"),
                            "http://localhost:3000/,http://localhost:5173/",
                            "http://localhost:5173/",
                            "authorization_code,refresh_token",
                            "openid,profile",
                            true
                    );
                    oauth2ClientRepository.save(client);
                });
    }
}
