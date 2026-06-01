import react from "@vitejs/plugin-react";
import { defineConfig, loadEnv } from "vite";

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), "");
    const portalApiBaseUrl = env.VITE_PORTAL_API_BASE_URL || "http://localhost:20020";
    const portalWebBase = env.VITE_PORTAL_WEB_BASE || "/";

    return {
        base: portalWebBase,
        plugins: [react()],
        resolve: {
            alias: {
                "@": "/src"
            }
        },
        test: {
            environment: "jsdom",
            globals: true,
            exclude: ["node_modules/**", "dist/**"]
        },
        server: {
            port: 5174,
            proxy: {
                "/portal-api": {
                    target: portalApiBaseUrl,
                    changeOrigin: true
                }
            }
        }
    };
});
