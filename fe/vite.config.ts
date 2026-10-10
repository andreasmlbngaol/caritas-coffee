import { fileURLToPath, URL } from "node:url";
import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

// Dev: Vite mem-proxy /api ke BE (same-origin dari sisi browser, cookie auth
// langsung jalan tanpa CORS). Produksi: nginx yang mem-proxy /api ke be:8080.
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    port: 5178,
    proxy: {
      "/api": {
        target: process.env.VITE_API_TARGET ?? "http://localhost:8091",
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: "dist",
    sourcemap: false,
  },
});
