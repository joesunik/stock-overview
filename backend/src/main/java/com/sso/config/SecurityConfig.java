package com.sso.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.sso.config.security.SaveRequestThenLoginEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import org.springframework.boot.web.servlet.FilterRegistrationBean;

/** OAuth2 인가 서버·로그인·API 보안 필터 체인 설정. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** /oauth2/* 등 인가 서버 엔드포인트. HTML 요청은 미인증 시 /login으로. */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (authorizationServer) ->
                        authorizationServer
                                .oidc(Customizer.withDefaults()))
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .anyRequest().authenticated())
                .requestCache((cache) -> cache.requestCache(new HttpSessionRequestCache()))
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new SaveRequestThenLoginEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
        // 동의 폼 POST는 서버가 렌더한 폼에서만 오며 redirect_uri 검증으로 보완. 토큰 엔드포인트는 BFF가 RestTemplate으로 호출하므로 CSRF 없음.
        http.csrf((csrf) -> csrf.ignoringRequestMatchers(
                new AntPathRequestMatcher("/oauth2/authorize", "POST"),
                new AntPathRequestMatcher("/oauth2/token", "POST")));

        return http.build();
    }

    /** /oauth2/authorize에 client_id 없이 접근 시 프론트로 리다이렉트. 반드시 Spring Security보다 먼저 실행되어야 하므로 HIGHEST_PRECEDENCE. */
    @Bean
    public FilterRegistrationBean<com.sso.config.security.RedirectInvalidAuthorizeFilter> redirectInvalidAuthorizeFilterRegistration(
            com.sso.config.security.RedirectInvalidAuthorizeFilter redirectInvalidAuthorizeFilter) {
        FilterRegistrationBean<com.sso.config.security.RedirectInvalidAuthorizeFilter> reg = new FilterRegistrationBean<>(redirectInvalidAuthorizeFilter);
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }

    /** /login, /error 등 로그인·에러 페이지. 폼 로그인 성공 시 loginSuccessHandler로 리다이렉트. */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
            AuthenticationSuccessHandler loginSuccessHandler,
            AuthenticationManager authenticationManager)
            throws Exception {
        http
                .securityMatcher(request -> {
                    String path = request.getServletPath();
                    return "/login".equals(path) || path.startsWith("/login?") || "/error".equals(path) || "/favicon.ico".equals(path);
                })
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
                .authenticationManager(authenticationManager)
                .formLogin((form) -> form.successHandler(loginSuccessHandler));

        return http.build();
    }

    /** /api/admin/** 는 JWT 검증 필요(리소스 서버). */
    @Bean
    @Order(3)
    public SecurityFilterChain apiAdminSecurityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder)
            throws Exception {
        http
                .securityMatcher("/api/admin/**")
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
                .oauth2ResourceServer((oauth2) -> oauth2.jwt((jwt) -> jwt.decoder(jwtDecoder)));

        return http.build();
    }

    /** 그 외 요청. /api/auth/** 는 인증 없이 허용(토큰 교환용), 나머지는 인증 필요. */
    @Bean
    @Order(4)
    public SecurityFilterChain apiAndOtherSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin((form) -> form.permitAll());
        // 프론트(3000)에서 프록시로 호출 시 8080 세션/CSRF 쿠키가 없어 403 방지
        http.csrf((csrf) -> csrf.ignoringRequestMatchers(
                new AntPathRequestMatcher("/api/auth/**")));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(com.sso.service.SsoUserDetailsService ssoUserDetailsService) {
        return ssoUserDetailsService;
    }

    /** 폼 로그인 전용 AuthenticationManager. parent 없이 DaoAuthenticationProvider만 사용해 재귀 위임으로 인한 StackOverflow 방지. */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    /** JWT 서명 검증용 RSA 키 소스(기동 시 2048비트 생성). */
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}
