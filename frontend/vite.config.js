import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    allowedHosts: 'all',
    proxy: {
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/voices': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/audio': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/tts': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/voice': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/feedback': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/stats': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/scripts': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/help': {
        target: 'http://localhost:8083',
        changeOrigin: true
      }
    }
  }
})