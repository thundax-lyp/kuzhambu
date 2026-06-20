import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), "");
    const adminApiBaseUrl = env.VITE_ADMIN_API_BASE_URL || "http://localhost:20010";
    const adminWebBase = env.VITE_ADMIN_WEB_BASE || (mode === "production" ? "/admin/" : "/");

    return {
        base: adminWebBase,
        plugins: [react()],
        resolve: {
            alias: {
                "@": "/src"
            }
        },
        test: {
            environment: "jsdom",
            globals: true,
            setupFiles: "./src/test/setup.ts",
            fileParallelism: false,
            exclude: ["e2e/**", "node_modules/**", "dist/**"]
        },
        build: {
            chunkSizeWarningLimit: 1500
        },
        server: {
            port: 5173,
            proxy: {
                "/kuzhambu-admin-api": {
                    target: adminApiBaseUrl,
                    changeOrigin: true
                }
            }
        }
    };
});
