import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/** 도메인 설정: OAuth2 클라이언트 목록 조회·redirect URI 수정. 인증 없으면 /login으로. */
export default function DomainSettingsPage() {
  const navigate = useNavigate();
  const { api, isAuthenticated, logout } = useAuth();
  const [clients, setClients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [editUris, setEditUris] = useState([]);

  // 인증 시에만 /api/admin/clients 호출; 401이면 /login으로
  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { replace: true });
      return;
    }
    api
      .get('/api/admin/clients')
      .then(({ data }) => setClients(Array.isArray(data) ? data : []))
      .catch((err) => {
        if (err.response?.status === 401) navigate('/login', { replace: true });
        else setError(err.message || '목록 조회 실패');
      })
      .finally(() => setLoading(false));
  }, [api, isAuthenticated, navigate]);

  const startEdit = useCallback((client) => {
    setEditingId(client.id);
    setEditUris([...(client.redirectUris || [])]);
  }, []);

  const cancelEdit = useCallback(() => {
    setEditingId(null);
    setEditUris([]);
  }, []);

  const addUri = useCallback(() => {
    setEditUris((prev) => [...prev, '']);
  }, []);

  const removeUri = useCallback((index) => {
    setEditUris((prev) => prev.filter((_, i) => i !== index));
  }, []);

  const changeUri = useCallback((index, value) => {
    setEditUris((prev) => {
      const next = [...prev];
      next[index] = value;
      return next;
    });
  }, []);

  /** PATCH로 redirect URIs 저장 후 목록 갱신. */
  const saveEdit = useCallback(() => {
    if (editingId == null) return;
    const redirectUris = editUris.map((u) => u.trim()).filter(Boolean);
    api
      .patch(`/api/admin/clients/${editingId}`, { redirectUris })
      .then(({ data }) => {
        setClients((prev) => prev.map((c) => (c.id === data.id ? data : c)));
        setEditingId(null);
        setEditUris([]);
      })
      .catch((err) => setError(err.response?.data?.message || err.message || '저장 실패'));
  }, [api, editingId, editUris]);

  if (loading) return <p>로딩 중...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
        <h1>도메인 설정</h1>
        <button type="button" onClick={logout}>로그아웃</button>
      </div>
      <p>OAuth2 클라이언트별 허용 redirect URI를 수정할 수 있습니다.</p>
      <ul style={{ listStyle: 'none', padding: 0 }}>
        {clients.map((client) => (
          <li key={client.id} style={{ border: '1px solid #ccc', marginBottom: '1rem', padding: '1rem' }}>
            <strong>클라이언트 ID:</strong> {client.clientId}
            {editingId === client.id ? (
              <div style={{ marginTop: '0.5rem' }}>
                {editUris.map((uri, i) => (
                  <div key={i} style={{ marginBottom: '0.25rem' }}>
                    <input
                      value={uri}
                      onChange={(e) => changeUri(i, e.target.value)}
                      placeholder="예: https://localhost:3000/"
                      style={{ width: '100%', maxWidth: '400px' }}
                    />
                    <button type="button" onClick={() => removeUri(i)}>삭제</button>
                  </div>
                ))}
                <button type="button" onClick={addUri}>URI 추가</button>
                <button type="button" onClick={saveEdit}>저장</button>
                <button type="button" onClick={cancelEdit}>취소</button>
              </div>
            ) : (
              <div style={{ marginTop: '0.5rem' }}>
                <strong>허용 리다이렉트 URI:</strong>
                <ul style={{ margin: 0, paddingLeft: '1.25rem' }}>
                  {(client.redirectUris || []).map((uri, i) => (
                    <li key={i}>{uri}</li>
                  ))}
                </ul>
                <button type="button" onClick={() => startEdit(client)}>수정</button>
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
