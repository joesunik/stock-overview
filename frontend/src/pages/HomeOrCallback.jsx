import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';

const REDIRECT_URI = typeof window !== 'undefined' ? `${window.location.origin}/` : 'http://localhost:3000/';

/** 루트(/) 처리: SSO 콜백(code)이 있으면 토큰 교환 후 /domains, 없으면 로그인 여부에 따라 이동. */
export default function HomeOrCallback() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { setTokens, isAuthenticated } = useAuth();

  useEffect(() => {
    const code = searchParams.get('code');
    if (code) {
      // 백엔드 BFF API로 코드→토큰 교환 후 토큰 저장 및 /domains로 이동
      axios
        .post('/api/auth/exchange', { code, redirectUri: REDIRECT_URI }, { headers: { 'Content-Type': 'application/json' } })
        .then(({ data }) => {
          setTokens(data.access_token, data.refresh_token);
          navigate('/domains', { replace: true });
          window.history.replaceState({}, '', '/domains');
        })
        .catch(() => {
          navigate('/login', { replace: true });
          window.history.replaceState({}, '', '/login');
        });
      return;
    }
    if (isAuthenticated) {
      navigate('/domains', { replace: true });
    } else {
      navigate('/login', { replace: true });
    }
  }, [searchParams, setTokens, navigate, isAuthenticated]);

  return <p>이동 중...</p>;
}
