import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// A build eredménye a Spring Boot statikus mappájába kerül: /react/index.html
export default defineConfig({
  plugins: [react()],
  base: '/react/',
  build: {
    outDir: '../target/classes/static/react',
    emptyOutDir: true
  },
  // fejlesztéskor (npm run dev) az /api hívásokat a Spring Boot kapja
  server: {
    proxy: { '/api': 'http://localhost:8081' }
  }
})
