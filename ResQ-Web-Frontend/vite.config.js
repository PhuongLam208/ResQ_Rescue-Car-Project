import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import rollupNodePolyFill from 'rollup-plugin-node-polyfills'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    fs: {
      strict: false
    },
    historyApiFallback: true
  },
  define: {
    global: 'window' // ✅ Fix cho lỗi "global is not defined"
  },
  optimizeDeps: {
    esbuildOptions: {
      define: {
        global: 'globalThis',
      },
    },
  },
  build: {
    rollupOptions: {
      plugins: [rollupNodePolyFill()],
    },
  }
})
