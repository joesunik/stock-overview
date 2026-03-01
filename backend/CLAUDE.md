# backend (SSO Server)

Spring Boot 3 + Java 21 기반 **SSO(단일 로그인) 서버** 모듈. OAuth2 Authorization Server로 동작하며, 클라이언트/사용자 정보는 Oracle DB에서 조회한다.

## 빌드 및 실행

- 루트에서: `mvn -pl backend spring-boot:run`
- backend 디렉터리에서: `cd backend && mvn spring-boot:run`
- 메인 클래스: `com.sso.SsoServerApplication`

## 데이터베이스

- **Oracle** 사용. `application.yml`의 `spring.datasource`(url: `jdbc:oracle:thin:@//host:port/service_name`), `spring.jpa.database-platform: org.hibernate.dialect.OracleDialect`.
- 환경 변수로 오버라이드: `ORACLE_USER`, `ORACLE_PASSWORD`, `ORACLE_SERVICE`.

## SSO 로그인 플로우

1. **프론트** (`/login`): 사용자가 "SSO 로그인" 클릭 → `window.location.href`로 **8080** `GET /oauth2/authorize?client_id=sso-client&redirect_uri=http://localhost:3000/&response_type=code&scope=openid profile` 이동 (vite 프록시 미사용).
2. **백엔드**: `RedirectInvalidAuthorizeFilter`(서블릿 필터, HIGHEST_PRECEDENCE) → 쿼리/client_id 없으면 403 대신 `app.frontend-url`로 리다이렉트. **이 필터는 반드시 Spring Security 필터보다 먼저 실행되어야 함.**
3. **인가 서버 체인**(Order 1): 미인증이면 `SaveRequestThenLoginEntryPoint`가 **`/oauth2/authorize`일 때만** RequestCache + **세션 속성 `sso.savedAuthorizeUrl`**에 전체 URL 저장 후 `/login`으로 리다이렉트.
4. **로그인 체인**(Order 2): `/login` 폼 제출 → `LoginSuccessHandler`에서 **`sso.savedAuthorizeUrl` 우선** 사용, 없으면 RequestCache에서 `/oauth2/authorize` 포함 URL 사용 → 저장된 **전체 URL**로 리다이렉트(동의 단계로 복귀).
5. **동의**: 이미 인증된 상태로 `/oauth2/authorize?...` 재요청 → Consent 화면 → Approve → **302** `http://localhost:3000/?code=...`.
6. **프론트** (`/`): `HomeOrCallback`이 `?code=` 있으면 **POST /api/auth/exchange** (code, redirectUri) → 토큰 수신 후 `AuthContext`에 저장, `/domains`로 이동.

저장 복원 시 **`sso.savedAuthorizeUrl`**을 쓰는 이유: 다른 요청(예: `.well-known/...?continue`)이 RequestCache를 덮어쓸 수 있어, 전용 세션 속성으로 인가 URL만 보존함.

## 패키지 구조


| 패키지                       | 설명                                                                              |
| ------------------------- | ------------------------------------------------------------------------------- |
| `com.sso`                 | 진입점 (`SsoServerApplication`)                                                    |
| `com.sso.domain`          | 엔티티: `SsoUser`, `OAuth2Client`                                                  |
| `com.sso.repository`      | JPA 리포지토리: `SsoUserRepository`, `OAuth2ClientRepository`                        |
| `com.sso.service`         | `SsoUserDetailsService`(UserDetails 로드), `TokenExchangeService`(코드↔토큰 교환)       |
| `com.sso.config`          | `SecurityConfig`, 기동 시 초기 데이터: `InitialUserRunner`, `InitialOAuth2ClientRunner` |
| `com.sso.config.security` | `LoginSuccessHandler`, `JpaRegisteredClientRepository`(DB 기반 OAuth2 클라이언트)      |
| `com.sso.web`             | REST API: `TokenController`, `AdminClientController`, `dto`                     |


## OAuth2 동작

- `OAuth2AuthorizationServerConfigurer` 사용. 클라이언트/사용자는 DB에서 로드 (`JpaRegisteredClientRepository`, `SsoUserDetailsService`).
- 로그인 성공 시: 저장된 요청이 `/oauth2/authorize`를 포함하면 해당 URL로 리다이렉트, 아니면 `app.frontend-url`(기본 `http://localhost:3000/`)로 이동.

## 테스트 계정·클라이언트

- 사용자: `admin` / `password` (기동 시 `InitialUserRunner`가 사용자가 없으면 1건 삽입).
- OAuth2 클라이언트: `sso-client`, redirect URI 3000/5173 등 (`InitialOAuth2ClientRunner`가 1건 삽입).

## BFF 토큰 API (프론트에서 호출)

- **POST `/api/auth/exchange`**: body `{ code, redirectUri }` → 액세스/리프레시 토큰 반환. 클라이언트는 DB에서 조회 (`TokenExchangeService`).
- **POST `/api/auth/refresh`**: body `{ refresh_token }` → 새 액세스(및 리프레시) 토큰 반환.

## 설정 파일

- `src/main/resources/application.yml`: DB, 포트, `app.frontend-url` 등.

