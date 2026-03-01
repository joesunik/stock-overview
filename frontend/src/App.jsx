import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import HomeOrCallback from './pages/HomeOrCallback';
import LoginPage from './pages/LoginPage';
import DomainSettingsPage from './pages/DomainSettingsPage';

/** 앱 루트: AuthProvider + 라우팅(/, /login, /domains). */
function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomeOrCallback />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/domains" element={<DomainSettingsPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
