import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig, loadEnv } from "vite";

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), "");
    const portalApiBaseUrl = env.VITE_PORTAL_API_BASE_URL || "http://localhost:20011";
    const portalWebBase = env.VITE_PORTAL_WEB_BASE || "/";

    return {
        base: portalWebBase,
        plugins: [react(), tailwindcss()],
        resolve: {
            alias: {
                "@": "/src"
            }
        },
        test: {
            environment: "jsdom",
            globals: true,
            exclude: ["e2e/**", "node_modules/**", "dist/**"],
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
            port: 5174,
            proxy: {
                "/kuzhambu-api": {
                    target: portalApiBaseUrl,
                    changeOrigin: true
                }
            }
        }
    };
});
