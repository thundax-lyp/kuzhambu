import { QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp, ConfigProvider, theme as antdTheme } from "antd";
import zhCN from "antd/locale/zh_CN";
import { useEffect, useState } from "react";
import { queryClient } from "./query/query-client";
import { AppRouter } from "./router";
import { getStoredTheme, subscribeAdminThemeChange } from "./theme/theme-storage";

const App = () => {
    const [themeName, setThemeName] = useState<"light" | "dark">(getStoredTheme);

    useEffect(() => {
        const syncTheme = () => setThemeName(getStoredTheme());
        return subscribeAdminThemeChange(syncTheme);
    }, []);

    useEffect(() => {
        document.documentElement.dataset.theme = themeName;
    }, [themeName]);

    return (
        <ConfigProvider
            locale={zhCN}
            theme={{
                algorithm:
                    themeName === "dark" ? antdTheme.darkAlgorithm : antdTheme.defaultAlgorithm,
                token: {
                    colorPrimary: themeName === "dark" ? "#3b82f6" : "#2563eb",
                    colorBgContainer: themeName === "dark" ? "#111c2e" : "#ffffff",
                    colorBorder:
                        themeName === "dark"
                            ? "rgba(171, 191, 222, 0.18)"
                            : "rgba(116, 132, 160, 0.24)",
                    colorText: themeName === "dark" ? "#f8fbff" : "#101827",
                    colorTextSecondary: themeName === "dark" ? "#b8c4d8" : "#526079",
                    borderRadius: 8,
                    fontFamily: '"Avenir Next", "PingFang SC", "Microsoft YaHei", sans-serif'
                }
            }}
        >
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <AppRouter />
                </AntdApp>
            </QueryClientProvider>
        </ConfigProvider>
    );
};

export default App;
