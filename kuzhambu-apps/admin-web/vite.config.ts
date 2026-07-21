import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), "");
    const adminServerPort = env.KUZHAMBU_ADMIN_SERVER_PORT || "20010";
    const adminApiBaseUrl = env.VITE_ADMIN_API_BASE_URL || `http://localhost:${adminServerPort}`;
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
            maxWorkers: "75%",
            coverage: {
                provider: "v8",
                reporter: ["text", "html", "lcov"],
                reportsDirectory: "coverage",
                include: ["src/**/*.{ts,tsx}"],
                exclude: [
                    "src/**/*.test.{ts,tsx}",
                    "src/**/*.spec.{ts,tsx}",
                    "src/test/**",
                    "src/**/*.d.ts",
                    "src/main.tsx"
                ]
            }
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
