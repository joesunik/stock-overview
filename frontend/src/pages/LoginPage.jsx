import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const SSO_CLIENT_ID = 'sso-client';
const SSO_AUTH_URL = 'http://localhost:8080/oauth2/authorize';
const REDIRECT_URI = typeof window !== 'undefined' ? `${window.location.origin}/` : 'http://localhost:3000/';

/** 로그인 페이지: 이미 로그인돼 있으면 /domains로, 버튼 클릭 시 SSO authorize로 이동. */
export default function LoginPage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/domains', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  /** SSO 서버 /oauth2/authorize로 code 스타일 인증 요청 리다이렉트. */
  const handleLogin = () => {
    const params = new URLSearchParams({
      client_id: SSO_CLIENT_ID,
      redirect_uri: REDIRECT_URI,
      response_type: 'code',
      scope: 'openid profile',
    });
    window.location.href = `${SSO_AUTH_URL}?${params.toString()}`;
  };

  return (
    <div style={{ padding: '2rem', textAlign: 'center' }}>
      <h1>로그인</h1>
      <p>SSO 서버로 로그인합니다.</p>
      <button type="button" onClick={handleLogin}>
        SSO 로그인
      </button>
    </div>
  );
}
