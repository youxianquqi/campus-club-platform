import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

/** 与后端 WebConfig 静态映射一致，便于 dev 下 img 使用相对路径 /club-logos 等 */
const apiProxy = {
  '/api': { target: 'http://localhost:8080', changeOrigin: true },
  '/club-logos': { target: 'http://localhost:8080', changeOrigin: true },
  '/activity-images': { target: 'http://localhost:8080', changeOrigin: true },
  '/avatars': { target: 'http://localhost:8080', changeOrigin: true }
}

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5174,
    proxy: { ...apiProxy }
  },
  // npm run preview 时也会走代理，否则请求会打到预览服务器本身导致非 JSON / 404
  preview: {
    port: 4174,
    proxy: { ...apiProxy }
  }
})
