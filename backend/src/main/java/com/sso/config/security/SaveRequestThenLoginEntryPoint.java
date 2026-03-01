package com.sso.config.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 로그인 페이지로 보내기 직전에 현재 요청을 세션에 저장한 뒤 /login으로 리다이렉트.
 * 인가 서버 체인에서 /oauth2/authorize 비인가 시 저장된 URL이 로그인 성공 후 복원되도록 함.
 * 다른 필터 체인이 RequestCache를 덮어쓸 수 있으므로, 인가 URL은 전용 세션 속성에도 저장함.
 */
public class SaveRequestThenLoginEntryPoint implements AuthenticationEntryPoint {

    /** 로그인 성공 후 복귀할 /oauth2/authorize URL을 넣는 세션 속성 (다른 체인의 RequestCache 덮어쓰기 방지). */
    public static final String SESSION_ATTR_SAVED_AUTHORIZE_URL = "sso.savedAuthorizeUrl";

    private static final String DEFAULT_LOGIN_URL = "/login";

    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final String loginFormUrl;

    public SaveRequestThenLoginEntryPoint() {
        this(DEFAULT_LOGIN_URL);
    }

    public SaveRequestThenLoginEntryPoint(String loginFormUrl) {
        this.loginFormUrl = loginFormUrl != null ? loginFormUrl : DEFAULT_LOGIN_URL;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        // 인가 엔드포인트 요청일 때만 저장. /.well-known 등 다른 요청이 세션 캐시를 덮어쓰지 않도록 함.
        if (request.getRequestURI() != null && request.getRequestURI().contains("/oauth2/authorize")) {
            requestCache.saveRequest(request, response);
            // 다른 체인이 RequestCache를 덮어써도 복원 가능하도록 전용 세션 속성에 URL 저장
            String fullUrl = request.getRequestURL().toString();
            if (request.getQueryString() != null) {
                fullUrl += "?" + request.getQueryString();
            }
            request.getSession(true).setAttribute(SESSION_ATTR_SAVED_AUTHORIZE_URL, fullUrl);
        }
        String redirectUrl = request.getContextPath() + (loginFormUrl.startsWith("/") ? loginFormUrl : "/" + loginFormUrl);
        response.sendRedirect(redirectUrl);
    }
}
