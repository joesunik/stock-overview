# stock-overview (SSO Server + Frontend)

- **모듈별 상세**: `backend/CLAUDE.md`, `frontend/CLAUDE.md` 참고.
- **코드 주석**: 한글 주석으로 LLM 이해 힌트 추가 규칙은 `.cursor/rules/korean-comments-llm-hints.mdc` 참고.

## Backend (Maven, Spring Boot 3, Java 21)
- Build/run: `cd backend && mvn spring-boot:run` or from root `mvn -pl backend spring-boot:run`.
- Main class: `com.sso.SsoServerApplication`.
- **DB**: Oracle. `application.yml`에 `spring.datasource`(url: `jdbc:oracle:thin:@//host:port/service_name`), `spring.jpa.database-platform: org.hibernate.dialect.OracleDialect`. 환경 변수 `ORACLE_USER`, `ORACLE_PASSWORD`, `ORACLE_SERVICE`로 오버라이드 가능.
- **패키지**: `com.sso`(진입점), `com.sso.domain`(SsoUser, OAuth2Client), `com.sso.repository`, `com.sso.service`(SsoUserDetailsService, TokenExchangeService), `com.sso.config`(Security, Runner), `com.sso.config.security`(LoginSuccessHandler, JpaRegisteredClientRepository), `com.sso.web`(TokenController).
- OAuth2: `new OAuth2AuthorizationServerConfigurer()`, 클라이언트/사용자는 DB에서 로드(JpaRegisteredClientRepository, SsoUserDetailsService).
- 로그인 성공 리다이렉트: 저장된 요청이 `/oauth2/authorize` 포함 시 해당 URL로, 아니면 `app.frontend-url`(기본 http://localhost:3000/).
- 테스트 계정: admin / password (기동 시 `InitialUserRunner`가 없으면 1건 삽입). OAuth2 클라이언트 sso-client(redirect 3000/5173)는 `InitialOAuth2ClientRunner`가 1건 삽입.
- BFF 토큰 API: POST `/api/auth/exchange`(code, redirectUri), POST `/api/auth/refresh`(refresh_token). 클라이언트 정보는 DB에서 조회(`TokenExchangeService`).

## SSO 로그인 플로우 (요약)

프론트 `/login` → 8080 `/oauth2/authorize?...` → 미인증 시 저장(`sso.savedAuthorizeUrl`) 후 `/login` → 로그인 성공 시 저장 URL로 복귀 → 동의 → 302 `/?code=...` → 프론트 POST `/api/auth/exchange` → 토큰 저장. 상세·필터 순서는 `backend/CLAUDE.md` 참고.

## Frontend (Vite, React, port 3000)
- API proxy: `/api` → backend :8080 (vite.config.js).
- SSO flow: redirect to backend /oauth2/authorize; callback exchanges code via POST /api/auth/exchange; refresh via POST /api/auth/refresh every 50 min.
