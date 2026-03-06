import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  // Base path must be '/' — Spring Boot serves from classpath:/static/
  base: '/',
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      // Proxy API calls to Spring Boot backend
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // Assets are served from frontend/public/assets/ by Vite directly.
      // No proxy needed — Vite serves public/ at root.
    },
  },
  build: {
    // Output to dist/ — frontend/pom.xml copies this to target/classes/static/
    outDir: 'dist',
    // IMPORTANT: Use '_app' instead of default 'assets' to avoid conflict
    // with /assets/** route used for game generated assets (sprites, atlases)
    assetsDir: '_app',
    target: 'es2020',
    minify: 'terser',
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: {
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],
          'animation': ['framer-motion'],
        },
      },
    },
  },
});
