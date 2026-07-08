import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const proxyOptions = target => ({ target, changeOrigin: true })

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api/auth': proxyOptions('http://localhost:8081'),
      '/api/voices': proxyOptions('http://localhost:8081'),
      '/api/audio': proxyOptions('http://localhost:8082'),
      '/api/tts': proxyOptions('http://localhost:8082'),
      '/api/voice': proxyOptions('http://localhost:8082'),
      '/api/scripts': proxyOptions('http://localhost:8083'),
      '/api/feedback': proxyOptions('http://localhost:8083'),
      '/api/stats': proxyOptions('http://localhost:8083'),
      '/api/help': proxyOptions('http://localhost:8083')
    }
  },
  build: {
    chunkSizeWarningLimit: 1300,
    rollupOptions: {
      output: {
        manualChunks: {
          vue: ['vue', 'vue-router', 'pinia'],
          element: ['element-plus', '@element-plus/icons-vue'],
          axios: ['axios']
        }
      }
    }
  }
})
