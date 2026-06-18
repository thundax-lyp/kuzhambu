import {
    AppstoreOutlined,
    ApiOutlined,
    AuditOutlined,
    BookOutlined,
    CloudServerOutlined,
    DownOutlined,
    FileTextOutlined,
    IdcardOutlined,
    LogoutOutlined,
    MenuFoldOutlined,
    MenuOutlined,
    MenuUnfoldOutlined,
    MoonOutlined,
    SafetyCertificateOutlined,
    SunOutlined,
    TeamOutlined,
    UserOutlined
} from "@ant-design/icons";
import { Alert, App, Avatar, Button, Dropdown, Layout, Menu, Space, Typography } from "antd";
import { useMutation, useQuery } from "@tanstack/react-query";
import type { MenuProps } from "antd";
import { useEffect, useState } from "react";
import type { ReactNode } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { logout } from "@/auth/auth-service";
import { refreshAccessTokenIfNeeded } from "../api/http";
import { useCurrentAccessToken } from "@/auth/hooks/use-current-access-token";
import { replacePermissions } from "../auth/permission-storage";
import { toAuthenticatedResourceUrl } from "../auth/resource-url";
import { clearAccessToken, getAccessToken } from "../auth/token-storage";
import { KuzhambuLogo } from "../components/kuzhambu-logo";
import {
    getCurrentUserInfo,
    listCurrentUserMenus,
    listCurrentUserPerms
} from "../service/current-user-service";
import { getStoredTheme, setAdminTheme, subscribeAdminThemeChange } from "../theme/theme-storage";

const { Header, Sider, Content } = Layout;
const { Text } = Typography;
const TOKEN_KEEP_ALIVE_INTERVAL_MS = 30 * 1000;

const menuIconMap: Record<string, ReactNode> = {
    dashboard: <AppstoreOutlined />,
    system: <SafetyCertificateOutlined />,
    users: <TeamOutlined />,
    roles: <SafetyCertificateOutlined />,
    menus: <MenuOutlined />,
    departments: <AppstoreOutlined />,
    dictionaries: <BookOutlined />,
    dictionary: <BookOutlined />,
    logs: <AuditOutlined />,
    audit: <AuditOutlined />,
    "audit-logs": <AuditOutlined />,
    storage: <CloudServerOutlined />,
    "storage-objects": <CloudServerOutlined />,
    submission: <FileTextOutlined />,
    submissions: <FileTextOutlined />,
    "open-api": <ApiOutlined />,
    permission: <SafetyCertificateOutlined />
};

const getOpenKeys = (pathname: string) => {
    const openKeys: string[] = [];

    if (pathname.startsWith("/system/")) {
        openKeys.push("/system");
    }

    if (pathname.startsWith("/storage/")) {
        openKeys.push("/storage");
    }

    if (pathname.startsWith("/audit/")) {
        openKeys.push("/audit");
    }

    return openKeys;
};

const normalizeMenuKey = (menu: { id: string; url?: string | null }) => {
    return menu.url || String(menu.id);
};

const getDisplayIcon = (displayParams?: string | null) => {
    if (!displayParams) {
        return undefined;
    }

    try {
        const parsedDisplayParams = JSON.parse(displayParams) as { icon?: unknown };
        return typeof parsedDisplayParams.icon === "string" ? parsedDisplayParams.icon : undefined;
    } catch {
        return undefined;
    }
};

const renderMenuIcon = (icon?: string | null, displayParams?: string | null) => {
    const configuredIcon = icon || getDisplayIcon(displayParams);
    const iconKey = configuredIcon?.trim();
    if (!iconKey) {
        return (
            <span className="menu-icon-config-error" title="菜单缺少 icon">
                !
            </span>
        );
    }

    return (
        menuIconMap[iconKey] || (
            <span className="menu-icon-config-error" title={`未知菜单 icon: ${iconKey}`}>
                !
            </span>
        )
    );
};

const buildAuthorizedMenuItems = (
    menus: Awaited<ReturnType<typeof listCurrentUserMenus>>
): MenuProps["items"] => {
    if (!menus.length) {
        return [];
    }

    const menuIds = new Set(menus.map((menu) => menu.id));
    const childrenByParentId = new Map<string | null, typeof menus>();
    menus.forEach((menu) => {
        const parentId =
            menu.parentId && menu.parentId !== menu.id && menuIds.has(menu.parentId)
                ? menu.parentId
                : null;
        const siblings = childrenByParentId.get(parentId) || [];
        siblings.push(menu);
        childrenByParentId.set(parentId, siblings);
    });

    const visited = new Set<string>();
    const toMenuItem = (
        menu: (typeof menus)[number],
        ancestors: Set<string> = new Set()
    ): NonNullable<MenuProps["items"]>[number] => {
        const key = normalizeMenuKey(menu);
        const nextAncestors = new Set(ancestors);
        nextAncestors.add(menu.id);
        visited.add(menu.id);
        const children = (childrenByParentId.get(menu.id) || []).filter(
            (child) => !nextAncestors.has(child.id)
        );

        return {
            key,
            icon: renderMenuIcon(menu.icon, menu.displayParams),
            label: menu.name,
            children: children.length
                ? children.map((child) => toMenuItem(child, nextAncestors))
                : undefined
        };
    };
    const rootMenus = childrenByParentId.get(null) || [];
    const rootMenuItems = rootMenus.map((menu) => toMenuItem(menu));
    const orphanMenuItems = menus
        .filter((menu) => !visited.has(menu.id))
        .map((menu) => toMenuItem(menu));

    return [
        ...(rootMenuItems as NonNullable<MenuProps["items"]>),
        ...(orphanMenuItems as NonNullable<MenuProps["items"]>)
    ];
};

export const AdminLayout = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { message: messageApi } = App.useApp();
    const [themeName, setThemeName] = useState<"light" | "dark">(getStoredTheme);
    const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
    const [isMobileLayout, setIsMobileLayout] = useState(false);
    const accessToken = useCurrentAccessToken();
    const currentUserInfoQuery = useQuery({
        queryKey: ["current-user", "info"],
        queryFn: getCurrentUserInfo,
        retry: false
    });
    const currentUserMenusQuery = useQuery({
        queryKey: ["current-user", "menus"],
        queryFn: listCurrentUserMenus,
        enabled: currentUserInfoQuery.isSuccess,
        retry: false
    });
    const currentUserPermsQuery = useQuery({
        queryKey: ["current-user", "perms"],
        queryFn: listCurrentUserPerms,
        enabled: currentUserInfoQuery.isSuccess,
        retry: false
    });
    const logoutMutation = useMutation({
        mutationFn: async () => {
            const token = getAccessToken();
            if (!token) {
                return false;
            }

            return logout({ token });
        },
        onSettled: () => {
            clearAccessToken();
            messageApi.success("已退出登录");
            navigate("/login", { replace: true });
        }
    });
    const menuItems = currentUserMenusQuery.data
        ? buildAuthorizedMenuItems(currentUserMenusQuery.data)
        : [];
    const currentUser = currentUserInfoQuery.data;
    const currentUserAvatar = toAuthenticatedResourceUrl(currentUser?.avatar, accessToken);
    const currentUserName = currentUser?.name || currentUser?.loginName || "当前用户";
    let sidebarState = sidebarCollapsed ? "collapsed" : "expanded";
    if (isMobileLayout) {
        sidebarState = sidebarCollapsed ? "closed" : "open";
    }

    useEffect(() => {
        if (currentUserPermsQuery.data) {
            replacePermissions(currentUserPermsQuery.data.perms || []);
        }
    }, [currentUserPermsQuery.data]);

    useEffect(() => {
        const syncTheme = () => setThemeName(getStoredTheme());
        return subscribeAdminThemeChange(syncTheme);
    }, []);

    useEffect(() => {
        void refreshAccessTokenIfNeeded();
        const timer = window.setInterval(() => {
            void refreshAccessTokenIfNeeded();
        }, TOKEN_KEEP_ALIVE_INTERVAL_MS);

        return () => window.clearInterval(timer);
    }, []);

    useEffect(() => {
        const mediaQueryList = window.matchMedia("(max-width: 760px)");
        const syncMobileLayout = (matches: boolean) => {
            setIsMobileLayout(matches);
            if (matches) {
                setSidebarCollapsed(true);
            }
        };
        const handleChange = (event: MediaQueryListEvent) => syncMobileLayout(event.matches);

        syncMobileLayout(mediaQueryList.matches);
        mediaQueryList.addEventListener("change", handleChange);
        return () => mediaQueryList.removeEventListener("change", handleChange);
    }, []);

    const toggleTheme = () => {
        const nextTheme = themeName === "dark" ? "light" : "dark";
        setThemeName(nextTheme);
        setAdminTheme(nextTheme);
    };
    const userMenuItems: MenuProps["items"] = [
        {
            key: "profile",
            icon: <IdcardOutlined />,
            label: "个人资料"
        },
        {
            type: "divider"
        },
        {
            key: "logout",
            icon: <LogoutOutlined />,
            label: "退出登录"
        }
    ];

    return (
        <Layout className="admin-shell">
            {isMobileLayout && !sidebarCollapsed ? (
                <button
                    className="sidebar-backdrop"
                    type="button"
                    aria-label="关闭菜单"
                    onClick={() => setSidebarCollapsed(true)}
                />
            ) : null}
            <Sider
                className={`sidebar${isMobileLayout && !sidebarCollapsed ? " sidebar-mobile-open" : ""}`}
                width={248}
                collapsedWidth={88}
                collapsed={isMobileLayout ? false : sidebarCollapsed}
                trigger={null}
            >
                <div className="sidebar-ink sidebar-ink-1" aria-hidden="true" />
                <div className="sidebar-ink sidebar-ink-2" aria-hidden="true" />
                <div className="brand">
                    <KuzhambuLogo className="brand-logo" />
                    <div className="brand-copy">
                        <strong>Kuzhambu</strong>
                        <span>Console</span>
                    </div>
                </div>

                <Menu
                    className="nav-menu"
                    mode="inline"
                    defaultOpenKeys={getOpenKeys(location.pathname)}
                    selectedKeys={[location.pathname]}
                    items={menuItems}
                    onClick={({ key }) => {
                        navigate(key);
                        if (isMobileLayout) {
                            setSidebarCollapsed(true);
                        }
                    }}
                />
            </Sider>

            <Layout className="admin-main" data-sidebar-state={sidebarState}>
                <Header className="topbar">
                    <div className="topbar-heading">
                        <Button
                            className="sidebar-toggle"
                            type="text"
                            shape="circle"
                            aria-label={sidebarCollapsed ? "展开菜单" : "收起菜单"}
                            icon={sidebarCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                            onClick={() => setSidebarCollapsed((collapsed) => !collapsed)}
                        />
                        <div>
                            <Text className="topbar-path">Kuzhambu Console</Text>
                        </div>
                    </div>
                    <Space className="topbar-actions">
                        <Button
                            shape="circle"
                            icon={themeName === "dark" ? <SunOutlined /> : <MoonOutlined />}
                            onClick={toggleTheme}
                        />
                        <Dropdown
                            menu={{
                                items: userMenuItems,
                                onClick: ({ key }) => {
                                    if (key === "logout") {
                                        logoutMutation.mutate();
                                    }
                                    if (key === "profile") {
                                        messageApi.info("个人资料功能待接入");
                                    }
                                }
                            }}
                            trigger={["click"]}
                        >
                            <Button
                                className="user-menu-trigger"
                                loading={logoutMutation.isPending}
                            >
                                <Avatar size={32} src={currentUserAvatar} icon={<UserOutlined />} />
                                <span className="user-menu-copy">
                                    <Text strong>{currentUserName}</Text>
                                    <Text type="secondary">
                                        {currentUser?.loginName || "未连接"}
                                    </Text>
                                </span>
                                <DownOutlined />
                            </Button>
                        </Dropdown>
                    </Space>
                </Header>

                <div className="admin-content-grid">
                    <Content className="workspace">
                        {currentUserInfoQuery.isError ? (
                            <Alert
                                type="warning"
                                showIcon
                                message="当前用户信息加载失败"
                                description="请确认当前登录态有效，并检查后台当前用户接口。"
                                style={{ marginBottom: 16 }}
                            />
                        ) : null}
                        {currentUserMenusQuery.isError ? (
                            <Alert
                                type="warning"
                                showIcon
                                message="权限菜单加载失败"
                                description="请确认当前登录态有效，并检查后台权限菜单接口。"
                                style={{ marginBottom: 16 }}
                            />
                        ) : null}
                        {currentUserPermsQuery.isError ? (
                            <Alert
                                type="warning"
                                showIcon
                                message="权限字符串加载失败"
                                description="请确认当前登录态有效，并检查后台当前用户权限接口。"
                                style={{ marginBottom: 16 }}
                            />
                        ) : null}
                        <Outlet />
                    </Content>
                </div>
            </Layout>
        </Layout>
    );
};
