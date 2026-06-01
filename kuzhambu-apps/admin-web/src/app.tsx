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
                    colorPrimary: themeName === "dark" ? "#7fa4c4" : "#1f4b73",
                    colorSuccess: themeName === "dark" ? "#7bb49e" : "#4f927a",
                    colorWarning: themeName === "dark" ? "#d6a75f" : "#b9863b",
                    colorError: themeName === "dark" ? "#d97867" : "#b33a32",
                    colorBgLayout: themeName === "dark" ? "#111713" : "#f7f4ec",
                    colorBgContainer: themeName === "dark" ? "#18211d" : "#fffdf8",
                    colorBorder:
                        themeName === "dark"
                            ? "rgba(184, 174, 150, 0.22)"
                            : "rgba(102, 89, 65, 0.18)",
                    colorText: themeName === "dark" ? "#f3eee3" : "#1f2521",
                    colorTextSecondary: themeName === "dark" ? "#c5bbab" : "#6d685d",
                    borderRadius: 8,
                    boxShadow: "0 12px 34px rgba(70, 58, 38, 0.08)",
                    boxShadowSecondary: "0 8px 24px rgba(70, 58, 38, 0.06)",
                    fontFamily:
                        '"Avenir Next", "LXGW WenKai", "STKaiti", "KaiTi", "PingFang SC", "Microsoft YaHei", sans-serif'
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
