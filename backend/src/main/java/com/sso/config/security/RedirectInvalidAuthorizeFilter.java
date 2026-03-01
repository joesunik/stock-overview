package com.sso.config.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * /oauth2/authorize에 client_id 등 필수 파라미터 없이 접근하면 403 대신 프론트로 리다이렉트.
 * 주의: 이 필터는 반드시 Spring Security 필터보다 먼저 실행되어야 함 (FilterRegistrationBean으로 HIGHEST_PRECEDENCE 등록).
 */
@Component
public class RedirectInvalidAuthorizeFilter extends OncePerRequestFilter {

    private final String frontendUrl;

    public RedirectInvalidAuthorizeFilter(@Value("${app.frontend-url:http://localhost:3000/}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path != null && path.contains("/oauth2/authorize")) {
            String query = request.getQueryString();
            String clientId = request.getParameter("client_id");
            // GET이면서 쿼리 없음 또는 client_id 없음이면 403 대신 프론트로 리다이렉트
            if ("GET".equalsIgnoreCase(request.getMethod()) && (query == null || query.isBlank() || clientId == null || clientId.isBlank())) {
                response.sendRedirect(frontendUrl);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
