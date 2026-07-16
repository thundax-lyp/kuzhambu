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
            pool: "threads",
            exclude: ["e2e/**", "node_modules/**", "dist/**"],
            testTimeout: 30000,
            hookTimeout: 30000,
            fileParallelism: true,
            maxWorkers: "75%"
        },
        build: {
            chunkSizeWarningLimit: 10000
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
