import { Moon, Search, Sun } from "lucide-react";
import { useEffect, useState, type CSSProperties } from "react";
import { Link, Outlet, useLocation } from "react-router-dom";
import kuzhambuLogoImage from "@/assets/home/kuzhambu-logo.svg";
import footerMountainImage from "@/assets/home/portal-home-effect-footer-mountain.png";
import { Button } from "@/components/ui/button";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import "./portal-effect-theme.css";
import "./portal-layout.css";

type PortalTheme = "light" | "dark";

const THEME_STORAGE_KEY = "kuzhambu.portal.theme";
const isTestIdExposed =
    import.meta.env.MODE !== "production" || import.meta.env.VITE_EXPOSE_TEST_ID === "true";

const footerBackgroundStyle = {
    "--portal-footer-bg-image": `url(${footerMountainImage})`
} as CSSProperties;

const getInitialTheme = (): PortalTheme => {
    if (typeof window === "undefined") {
        return "light";
    }

    const storedTheme = window.localStorage.getItem(THEME_STORAGE_KEY);

    if (storedTheme === "dark" || storedTheme === "light") {
        return storedTheme;
    }

    if (typeof window.matchMedia === "function") {
        return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
    }

    return "light";
};

const navigationItems = [
    { label: "三才图会", href: "/classics/sancai" },
    { label: "知识图谱", href: "/knowledge" },
    { label: "古籍检索", href: "/discovery/search" },
    { label: "公开分享", href: "/shares" },
    { label: "问答", href: "/discovery/qa" }
];

const footerGroups = [
    {
        title: "资源导航",
        links: ["三才图会", "山书与美书", "地方志", "金石碑刻"]
    },
    {
        title: "服务",
        links: ["古籍检索", "知识图谱", "图像浏览", "数据开放"]
    },
    {
        title: "研究支持",
        links: ["研究指南", "引用规范", "版本说明", "常见问题"]
    },
    {
        title: "社区与协作",
        links: ["公开分享", "问答", "整理与校勘协作", "意见反馈"]
    },
    {
        title: "关于我们",
        links: ["项目介绍", "团队", "版权与声明", "联系方式"]
    }
];

export const PortalLayout = () => {
    const location = useLocation();
    const [theme, setTheme] = useState<PortalTheme>(getInitialTheme);
    const isThemeSupportedRoute = location.pathname === "/";
    const isDarkTheme = theme === "dark";
    const themeToggleLabel = isDarkTheme ? "切换浅色主题" : "切换深色主题";

    useEffect(() => {
        document.documentElement.classList.toggle("dark", isThemeSupportedRoute && isDarkTheme);
    }, [isDarkTheme, isThemeSupportedRoute]);

    useEffect(() => {
        if (!isThemeSupportedRoute) {
            return;
        }

        window.localStorage.setItem(THEME_STORAGE_KEY, theme);
    }, [isThemeSupportedRoute, theme]);

    const handleThemeToggle = () => {
        setTheme((currentTheme) => (currentTheme === "dark" ? "light" : "dark"));
    };

    return (
        <div className="portal-shell portal-effect-layout">
            <TooltipProvider>
                <header className="portal-effect-header">
                    <Link className="portal-effect-brand" to="/" aria-label="Kuzhambu 首页">
                        <span>Kuzhambu</span>
                        <img className="portal-effect-brand-logo" src={kuzhambuLogoImage} alt="" />
                    </Link>
                    <nav className="portal-effect-nav" aria-label="门户导航">
                        {navigationItems.map((item) => (
                            <Link key={item.href} to={item.href}>
                                {item.label}
                            </Link>
                        ))}
                    </nav>
                    <div className="portal-effect-header-actions">
                        <Link
                            className="portal-effect-search"
                            to="/discovery/search"
                            aria-label="进入古籍检索"
                        >
                            <Search aria-hidden="true" size={17} />
                            <span>搜索条目、图像、人物、地名、典籍...</span>
                            <strong>搜索</strong>
                        </Link>
                        {isThemeSupportedRoute ? (
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <Button
                                        type="button"
                                        variant="outline"
                                        size="icon-lg"
                                        className="portal-effect-theme-toggle"
                                        aria-label={themeToggleLabel}
                                        aria-pressed={isDarkTheme}
                                        onClick={handleThemeToggle}
                                        {...(isTestIdExposed
                                            ? { "data-testid": "portal-header-theme-toggle" }
                                            : {})}
                                    >
                                        {isDarkTheme ? (
                                            <Sun aria-hidden="true" data-icon="inline-start" />
                                        ) : (
                                            <Moon aria-hidden="true" data-icon="inline-start" />
                                        )}
                                    </Button>
                                </TooltipTrigger>
                                <TooltipContent>{themeToggleLabel}</TooltipContent>
                            </Tooltip>
                        ) : null}
                    </div>
                </header>
            </TooltipProvider>

            <Outlet />

            <footer className="portal-effect-footer" style={footerBackgroundStyle}>
                <div className="portal-effect-footer-intro">
                    <img className="portal-effect-brand-logo" src={kuzhambuLogoImage} alt="" />
                    <p>汇聚古籍与图像资源，连接知识与研究线索。</p>
                    <Link to="/knowledge">关于KUZHAMBU</Link>
                </div>
                <nav className="portal-effect-footer-nav" aria-label="底部资源导航">
                    {footerGroups.map((group) => (
                        <section key={group.title}>
                            <h2>{group.title}</h2>
                            <div className="portal-effect-footer-nav-links">
                                {group.links.map((link) => (
                                    <Link key={link} to="/discovery/search">
                                        {link}
                                    </Link>
                                ))}
                            </div>
                        </section>
                    ))}
                </nav>
            </footer>
        </div>
    );
};
