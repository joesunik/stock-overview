# frontend (Stock Overview)

Vite + React 기반 프론트엔드. 포트 **3000**에서 동작하며, 백엔드 SSO 서버와 연동해 로그인·토큰 갱신을 처리한다.

## 실행

- `npm run dev` (또는 `npm run build` 후 `npm run preview`).
- API는 `/api`로 요청 시 `vite.config.js`의 proxy를 통해 백엔드(기본 :8080)로 전달된다.

## 디렉터리 구조

| 경로 | 설명 |
|------|------|
| `src/main.jsx` | 앱 진입점, 라우터 및 `AuthProvider` 래핑 |
| `src/App.jsx` | 루트 UI (Stock Overview 등) |
| `src/context/AuthContext.jsx` | 로그인 상태·토큰·API 클라이언트 제공 (`useAuth`) |
| `src/pages/LoginPage.jsx` | 로그인 페이지 (SSO authorize 리다이렉트 유도) |
| `src/pages/HomeOrCallback.jsx` | `/` 처리: SSO 콜백(code)이 있으면 토큰 교환 후 `/domains`, 없으면 로그인 여부에 따라 `/domains` 또는 `/login` |

## SSO 플로우

1. 미인증 사용자가 보호된 경로 접근 시 → `/login`으로 유도.
2. 로그인 페이지에서 백엔드 `/oauth2/authorize`로 리다이렉트.
3. 백엔드에서 로그인 후 콜백으로 `/?code=...` 반환.
4. `HomeOrCallback`에서 **POST `/api/auth/exchange`**로 `code`·`redirectUri` 전달 → 액세스/리프레시 토큰 수신 후 `AuthContext`에 저장.
5. 액세스 토큰은 `sessionStorage` 및 `AuthContext`의 `api`(axios 인스턴스)에서 Bearer로 자동 부착. 401 시 **POST `/api/auth/refresh`**로 갱신(예: 50분 주기).

## 주요 의존성

- React 19, React Router DOM, Axios. 빌드/번들: Vite, @vitejs/plugin-react.
