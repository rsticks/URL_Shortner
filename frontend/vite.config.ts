import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import type { ProxyOptions } from 'vite'

const apiProxy: ProxyOptions = {
  target: 'http://127.0.0.1:8080',
  changeOrigin: true,
  secure: false,
  configure(proxy) {
    proxy.on('error', (err, _req, _res) => {
      console.error('Vite API proxy error:', err)
    })
  },
}

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': apiProxy,
    },
  },
})
