package com.sso.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** 로그인 성공 시: 저장된 요청이 /oauth2/authorize면 그 URL로, 아니면 app.frontend-url로 리다이렉트. */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final String frontendUrl;
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    public LoginSuccessHandler(@Value("${app.frontend-url:http://localhost:3000/}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    /** 세션에 저장된 요청 URL이 있으면 그쪽으로, 없으면 프론트 URL로 보냄. 전용 세션 속성(sso.savedAuthorizeUrl) 우선 사용. */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            org.springframework.security.core.Authentication authentication) {
        String redirectUrl = frontendUrl;
        // 다른 체인이 RequestCache를 덮어쓴 경우를 위해, 인가 URL 전용 세션 속성 우선 확인
        String savedAuthorizeUrl = null;
        if (request.getSession(false) != null) {
            savedAuthorizeUrl = (String) request.getSession().getAttribute(SaveRequestThenLoginEntryPoint.SESSION_ATTR_SAVED_AUTHORIZE_URL);
            if (savedAuthorizeUrl != null) {
                request.getSession().removeAttribute(SaveRequestThenLoginEntryPoint.SESSION_ATTR_SAVED_AUTHORIZE_URL);
                redirectUrl = savedAuthorizeUrl;
            }
        }
        if (redirectUrl == frontendUrl) {
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            if (savedRequest != null) {
                String savedUrl = savedRequest.getRedirectUrl();
                if (savedUrl != null && savedUrl.contains("/oauth2/authorize")) {
                    redirectUrl = savedUrl;
                }
            }
        }
        // 복원된 URL에 client_id가 없으면 403 방지를 위해 프론트로 보냄
        if (redirectUrl != null && redirectUrl.contains("/oauth2/authorize") && !redirectUrl.contains("client_id")) {
            redirectUrl = frontendUrl;
        }
        try {
            response.sendRedirect(redirectUrl);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Redirect failed", e);
        }
    }
}
