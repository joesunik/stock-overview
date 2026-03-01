import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import axios from 'axios';

// 세션 스토리지 키: 액세스/리프레시 토큰
const STORAGE_ACCESS = 'sso_access_token';
const STORAGE_REFRESH = 'sso_refresh_token';

const AuthContext = createContext(null);

/** 로그인 상태·토큰·API(axios) 제공. 401 시 리프레시 후 재요청. */
export function AuthProvider({ children }) {
  const [accessToken, setAccessTokenState] = useState(() => sessionStorage.getItem(STORAGE_ACCESS));
  const [refreshToken, setRefreshTokenState] = useState(() => sessionStorage.getItem(STORAGE_REFRESH));

  const setTokens = useCallback((access, refresh) => {
    if (access) {
      sessionStorage.setItem(STORAGE_ACCESS, access);
      setAccessTokenState(access);
    } else {
      sessionStorage.removeItem(STORAGE_ACCESS);
      setAccessTokenState(null);
    }
    if (refresh) {
      sessionStorage.setItem(STORAGE_REFRESH, refresh);
      setRefreshTokenState(refresh);
    } else {
      sessionStorage.removeItem(STORAGE_REFRESH);
      setRefreshTokenState(null);
    }
  }, []);

  const logout = useCallback(() => {
    setTokens(null, null);
  }, [setTokens]);

  const refreshAccessToken = useCallback(async () => {
    const r = refreshToken || sessionStorage.getItem(STORAGE_REFRESH);
    if (!r) return false;
    try {
      // POST /api/auth/refresh로 새 액세스 토큰 발급
      const { data } = await axios.post('/api/auth/refresh', { refresh_token: r }, {
        headers: { 'Content-Type': 'application/json' },
      });
      const access = data.access_token;
      const ref = data.refresh_token;
      if (access) setTokens(access, ref ?? r);
      return !!access;
    } catch {
      logout();
      return false;
    }
  }, [refreshToken, setTokens, logout]);

  const isAuthenticated = !!accessToken;

  const api = useMemo(() => {
    const instance = axios.create({ baseURL: '', headers: {} });
    // 요청 시 세션의 액세스 토큰을 Bearer로 자동 부착
    instance.interceptors.request.use((config) => {
      const token = sessionStorage.getItem(STORAGE_ACCESS) || accessToken;
      if (token) config.headers.Authorization = `Bearer ${token}`;
      return config;
    });
    instance.interceptors.response.use(
      (res) => res,
      // 401이면 리프레시 시도 후 원래 요청 재전송
      async (err) => {
        if (err.response?.status === 401) {
          const ok = await refreshAccessToken();
          if (ok) return instance.request(err.config);
        }
        return Promise.reject(err);
      }
    );
    return instance;
  }, [accessToken, refreshAccessToken]);

  const value = useMemo(() => ({
    accessToken,
    refreshToken,
    setTokens,
    logout,
    refreshAccessToken,
    isAuthenticated,
    api,
  }), [accessToken, refreshToken, setTokens, logout, refreshAccessToken, isAuthenticated, api]);

  // 50분마다 리프레시 토큰으로 액세스 토큰 갱신
  useEffect(() => {
    if (!refreshToken && !sessionStorage.getItem(STORAGE_REFRESH)) return;
    const intervalMs = 50 * 60 * 1000;
    const id = setInterval(refreshAccessToken, intervalMs);
    return () => clearInterval(id);
  }, [refreshToken, refreshAccessToken]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

/** AuthContext 사용. AuthProvider 밖에서 쓰면 예외. */
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth는 AuthProvider 내부에서만 사용할 수 있습니다.');
  return ctx;
}
