import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// 개발 서버 포트 3000, /api 요청은 백엔드 8080으로 프록시
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
