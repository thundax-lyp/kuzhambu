import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AdminQueryProvider } from "@/query/query-client";
import { App as AntdApp } from "antd";
import type { ReactNode } from "react";
import App from "./app";
import { clearPermissions, hasPermission, replacePermissions } from "./auth/permission-storage";
import { KuzhambuTable } from "./components/kuzhambu-table";
import { AuditLogPage } from "./pages/audit/audit-log/audit-log-page";
import { StorageObjectPage } from "./pages/storage/storage-object/storage-object-page";
import { DepartmentPage } from "./pages/system/department/department-page";
import { DictionaryPage } from "./pages/system/dictionary/dictionary-page";
import { MenuPage } from "./pages/system/menu/menu-page";
import { UserPage } from "./pages/system/user/user-page";
import { getCurrentUserInfo, listCurrentUserMenus } from "./service/current-user-service";

vi.mock("sm-crypto", () => ({
    sm2: {
        doEncrypt: () => "encrypted-password"
    }
}));

describe("App", () => {
    const renderWithQueryClient = (node: ReactNode) => {
        render(<AdminQueryProvider>{node}</AdminQueryProvider>);
    };

    const renderWithQueryClientAndApp = (node: ReactNode) => {
        render(
            <AdminQueryProvider>
                <AntdApp>{node}</AntdApp>
            </AdminQueryProvider>
        );
    };

    beforeEach(() => {
        localStorage.clear();
        clearPermissions();
        window.history.pushState({}, "", "/");
    });

    afterEach(() => {
        cleanup();
        vi.restoreAllMocks();
    });

    it("redirects protected routes to login without a token", async () => {
        render(<App />);

        expect(await screen.findByRole("heading", { name: "登录" })).toBeInTheDocument();
    });

    it("renders the admin dashboard route", async () => {
        const user = userEvent.setup({ delay: null });
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const url = String(input);
            if (url.endsWith("/sys/current-user/get")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                id: "1",
                                loginName: "developer",
                                name: "Developer"
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/menu/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: [
                                { id: "10", name: "系统管理", displayParams: '{"icon":"system"}' },
                                {
                                    id: "11",
                                    parentId: "10",
                                    name: "用户管理",
                                    icon: "users",
                                    url: "/system/users"
                                },
                                {
                                    id: "20",
                                    name: "古籍管理",
                                    displayParams: '{"icon":"classics"}'
                                },
                                {
                                    id: "21",
                                    parentId: "20",
                                    name: "三才图会",
                                    displayParams: '{"icon":"sancai"}',
                                    url: "/classics/sancai"
                                },
                                {
                                    id: "30",
                                    name: "知识治理",
                                    displayParams: '{"icon":"knowledge"}'
                                },
                                {
                                    id: "31",
                                    parentId: "30",
                                    name: "知识图谱",
                                    permission: "knowledge:graph:view",
                                    displayParams: '{"icon":"knowledge-graph"}',
                                    url: "/knowledge/graph"
                                }
                            ]
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/permission/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                perms: ["sys:user:view", "sys:user:edit", "knowledge:graph:view"]
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            return Promise.resolve(
                new Response(
                    JSON.stringify({
                        code: "COMMON-00004",
                        message: "not found"
                    }),
                    {
                        headers: { "Content-Type": "application/json" },
                        status: 404
                    }
                )
            );
        });

        render(<App />);

        expect(
            await screen.findByRole("heading", { name: "仪表盘" }, { timeout: 5000 })
        ).toBeInTheDocument();
        expect(await screen.findByText("Developer")).toBeInTheDocument();
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/kuzhambu-admin-api/api/sys/current-user/get",
            expect.objectContaining({
                headers: expect.objectContaining({
                    "Access-Token": "test-token"
                }),
                method: "POST"
            })
        );
        expect(await screen.findByText("系统管理")).toBeInTheDocument();
        expect(document.querySelector(".anticon-safety-certificate")).toBeInTheDocument();
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/kuzhambu-admin-api/api/sys/current-user/menu/list",
            expect.objectContaining({
                headers: expect.objectContaining({
                    "Access-Token": "test-token"
                }),
                method: "POST"
            })
        );
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/kuzhambu-admin-api/api/sys/current-user/permission/list",
            expect.objectContaining({
                headers: expect.objectContaining({
                    "Access-Token": "test-token"
                }),
                method: "POST"
            })
        );
        await waitFor(() => expect(hasPermission("sys:user:view")).toBe(true));
        expect(hasPermission("sys:role:edit")).toBe(false);
        expect(await screen.findByText("古籍管理")).toBeInTheDocument();
        expect(await screen.findByText("知识治理")).toBeInTheDocument();
        await user.click(await screen.findByRole("menuitem", { name: /知识治理/ }));
        expect(await screen.findByText("知识图谱")).toBeInTheDocument();
    });

    it("loads permissions as part of successful login", async () => {
        const user = userEvent.setup({ delay: null });
        const loginExpireAt = Date.now() + 5 * 60 * 1000;
        vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
            const url = String(input);
            if (url.endsWith("/auth/session/pre-auth-session/request")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                loginToken: "login-form-token",
                                refreshToken: "refresh-token",
                                expiredAt: 1778513052155,
                                publicKey: "public-key"
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/auth/session/login")) {
                expect(init).toEqual(
                    expect.objectContaining({
                        body: JSON.stringify({
                            loginToken: "login-form-token",
                            userName: "developer",
                            password: "encrypted-password",
                            captcha: "1234"
                        })
                    })
                );

                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                token: "login-access-token",
                                refreshToken: "login-refresh-token",
                                expireAt: loginExpireAt
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/permission/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                perms: ["sys:user:view", "sys:user:edit"]
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/get")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                id: "1",
                                loginName: "developer",
                                name: "Developer"
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/menu/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({ code: "COMMON-00000", message: "success", data: [] }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: "not found" }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        render(<App />);

        await user.type(await screen.findByPlaceholderText("请输入后台账号"), "developer");
        await user.type(screen.getByPlaceholderText("请输入密码"), "kuzhambu");
        await user.type(screen.getByPlaceholderText("验证码"), "1234");
        await user.click(screen.getByRole("button", { name: /登\s*录/ }));

        expect(
            await screen.findByRole("heading", { name: "仪表盘" }, { timeout: 5000 })
        ).toBeInTheDocument();
        expect(localStorage.getItem("kuzhambu.admin.accessToken")).toBe("login-access-token");
        expect(localStorage.getItem("kuzhambu.admin.refreshToken")).toBe("login-refresh-token");
        expect(localStorage.getItem("kuzhambu.admin.accessTokenExpireAt")).toBe(
            String(loginExpireAt)
        );
        await waitFor(() => expect(hasPermission("sys:user:view")).toBe(true));
        expect(hasPermission("sys:role:view")).toBe(false);
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/kuzhambu-admin-api/api/sys/current-user/permission/list",
            expect.objectContaining({
                headers: expect.objectContaining({
                    "Access-Token": "login-access-token"
                }),
                method: "POST"
            })
        );
    }, 30000);

    it("logs out and returns to the login route", async () => {
        const user = userEvent.setup({ delay: null });
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const url = String(input);
            if (url.endsWith("/sys/current-user/get")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                id: "1",
                                loginName: "developer",
                                name: "Developer"
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/menu/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({ code: "COMMON-00000", message: "success", data: [] }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/permission/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                perms: ["sys:user:view"]
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            return Promise.resolve(
                new Response(
                    JSON.stringify({ code: "COMMON-00000", message: "success", data: true }),
                    {
                        headers: { "Content-Type": "application/json" },
                        status: 200
                    }
                )
            );
        });

        render(<App />);

        await user.click(await screen.findByRole("button", { name: /Developer/ }));
        await user.click(await screen.findByRole("menuitem", { name: /退出登录/ }));

        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/kuzhambu-admin-api/api/auth/session/logout",
            expect.objectContaining({
                body: JSON.stringify({ token: "test-token" }),
                headers: expect.objectContaining({
                    "Access-Token": "test-token"
                }),
                method: "POST"
            })
        );
        expect(localStorage.getItem("kuzhambu.admin.accessToken")).toBeNull();
        expect(await screen.findByRole("heading", { name: "登录" })).toBeInTheDocument();
    });

    it("renders the department list page", async () => {
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.permissions",
            JSON.stringify(["sys:department:view", "sys:department:edit"])
        );
        replacePermissions(["sys:department:view", "sys:department:edit"]);
        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const url = String(input);
            if (url.endsWith("/sys/department/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: [
                                {
                                    id: "1",
                                    name: "总部",
                                    shortName: "HQ",
                                    namePath: "总部",
                                    remarks: "核心组织"
                                },
                                {
                                    id: "2",
                                    parentId: "1",
                                    name: "技术部",
                                    shortName: "Tech",
                                    namePath: "总部/技术部"
                                }
                            ]
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: "not found" }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        renderWithQueryClientAndApp(<DepartmentPage />);

        expect(await screen.findByRole("heading", { name: "部门管理" })).toBeInTheDocument();
        expect((await screen.findAllByText("总部")).length).toBeGreaterThan(0);
    }, 30000);

    it("blocks department list page without view permission", async () => {
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem("kuzhambu.admin.permissions", JSON.stringify([]));
        replacePermissions([]);

        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const url = String(input);
            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: `mocked ${url}` }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        renderWithQueryClientAndApp(<DepartmentPage />);

        expect(await screen.findByText("缺少 sys:department:view 权限")).toBeInTheDocument();
    }, 30000);

    it("blocks menu page without super permission", async () => {
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem("kuzhambu.admin.permissions", JSON.stringify(["sys:menu:view"]));
        replacePermissions(["sys:menu:view"]);

        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const url = String(input);
            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: `mocked ${url}` }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        renderWithQueryClientAndApp(<MenuPage />);

        expect(await screen.findByText("缺少 super 权限")).toBeInTheDocument();
        expect(globalThis.fetch).not.toHaveBeenCalled();
    }, 30000);

    it("renders and filters the dictionary page", async () => {
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.permissions",
            JSON.stringify(["sys:dict:view", "sys:dict:edit"])
        );
        replacePermissions(["sys:dict:view", "sys:dict:edit"]);
        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const url = String(input);
            if (url.endsWith("/sys/dict/page")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                pageNo: 1,
                                pageSize: 20,
                                totalPage: 1,
                                totalCount: 2,
                                records: [
                                    {
                                        id: "1",
                                        type: "user_status",
                                        label: "启用",
                                        value: "ENABLED",
                                        remarks: "允许登录"
                                    },
                                    {
                                        id: "2",
                                        type: "user_status",
                                        label: "停用",
                                        value: "DISABLED"
                                    }
                                ]
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: "not found" }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        renderWithQueryClientAndApp(<DictionaryPage />);

        expect(await screen.findByRole("heading", { name: "字典管理" })).toBeInTheDocument();
        expect(await screen.findByText("启用")).toBeInTheDocument();
        expect(screen.getByText("DISABLED")).toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: /筛选/ }));
        await userEvent.type(screen.getByPlaceholderText("user_status"), "user_status");
        await userEvent.click(screen.getByRole("button", { name: /查\s*询/ }));

        await waitFor(() =>
            expect(globalThis.fetch).toHaveBeenLastCalledWith(
                "/kuzhambu-admin-api/api/sys/dict/page",
                expect.objectContaining({
                    body: JSON.stringify({
                        type: "user_status",
                        pageNo: 1,
                        pageSize: 20
                    }),
                    headers: expect.objectContaining({
                        "Access-Token": "test-token"
                    }),
                    method: "POST"
                })
            )
        );
    }, 30000);

    it("renders the audit log page", async () => {
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem("kuzhambu.admin.permissions", JSON.stringify(["audit:view"]));
        replacePermissions(["audit:view"]);
        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const url = String(input);
            if (url.endsWith("/audit/log/options/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                objectTypes: [{ value: "SUBMISSION", label: "提交内容" }],
                                actions: [{ value: "UPDATE", label: "更新" }],
                                operatorTypes: [{ value: "USER", label: "后台用户" }]
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/audit/log/page")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                pageNo: 1,
                                pageSize: 20,
                                totalPage: 1,
                                totalCount: 1,
                                records: [
                                    {
                                        id: "9001",
                                        objectType: "SUBMISSION",
                                        objectTypeLabel: "提交内容",
                                        objectId: "1001",
                                        objectDisplayName: "产品反馈",
                                        version: 2,
                                        action: "UPDATE",
                                        actionLabel: "更新",
                                        operatorType: "USER",
                                        operatorId: "1000000000000000101",
                                        operatorTypeLabel: "后台用户",
                                        operatorName: "Developer",
                                        source: "ADMIN_WEB",
                                        summary: "更新提交内容",
                                        occurredAt: "2026-05-19T10:00:00.000+08:00"
                                    }
                                ]
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/audit/log/detail/get")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                id: "9001",
                                objectType: "SUBMISSION",
                                objectTypeLabel: "提交内容",
                                objectId: "1001",
                                objectDisplayName: "产品反馈",
                                version: 2,
                                action: "UPDATE",
                                actionLabel: "更新",
                                operatorType: "USER",
                                operatorId: "1000000000000000101",
                                operatorTypeLabel: "后台用户",
                                operatorName: "Developer",
                                source: "ADMIN_WEB",
                                summary: "更新提交内容",
                                occurredAt: "2026-05-19T10:00:00.000+08:00",
                                changedFields: [
                                    {
                                        fieldName: "title",
                                        fieldLabel: "标题",
                                        beforeDisplayValue: "旧标题",
                                        afterDisplayValue: "新标题"
                                    }
                                ],
                                beforeSnapshot: {
                                    fields: [
                                        {
                                            fieldName: "title",
                                            fieldLabel: "标题",
                                            displayValue: "旧标题"
                                        },
                                        {
                                            fieldName: "status",
                                            fieldLabel: "状态",
                                            displayValue: "待处理"
                                        }
                                    ]
                                },
                                afterSnapshot: {
                                    fields: [
                                        {
                                            fieldName: "title",
                                            fieldLabel: "标题",
                                            displayValue: "新标题"
                                        },
                                        {
                                            fieldName: "status",
                                            fieldLabel: "状态",
                                            displayValue: "待处理"
                                        }
                                    ]
                                }
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: "not found" }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        renderWithQueryClient(<AuditLogPage />);

        expect(await screen.findByRole("heading", { name: "审计日志" })).toBeInTheDocument();
        expect(await screen.findByText("产品反馈")).toBeInTheDocument();
        expect(screen.getByText("提交内容")).toBeInTheDocument();
        expect(screen.getByText("更新")).toBeInTheDocument();
        expect(screen.getByText("Developer")).toBeInTheDocument();
        expect(screen.queryByRole("columnheader", { name: "字段" })).not.toBeInTheDocument();
        await waitFor(() => {
            expect(
                Array.from(document.querySelectorAll("img")).some((image) =>
                    image.src.endsWith(
                        "/kuzhambu-admin-api/api/sys/user/avatar?id=1000000000000000101&token=test-token"
                    )
                )
            ).toBe(true);
        });
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/kuzhambu-admin-api/api/audit/log/page",
            expect.objectContaining({
                body: JSON.stringify({
                    pageNo: 1,
                    pageSize: 20
                }),
                headers: expect.objectContaining({
                    "Access-Token": "test-token"
                }),
                method: "POST"
            })
        );

        await userEvent.click(screen.getByRole("button", { name: "查看审计日志 9001" }));

        expect(await screen.findByText("快照对比")).toBeInTheDocument();
        expect(screen.getAllByText("旧标题").length).toBeGreaterThan(0);
        expect(screen.getAllByText("新标题").length).toBeGreaterThan(0);
        expect(screen.getByText("已变更")).toBeInTheDocument();
    }, 30000);

    it("opens authenticated storage content urls and refreshes after delete", async () => {
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.permissions",
            JSON.stringify(["storage:object:view", "storage:object:edit"])
        );
        replacePermissions(["storage:object:view", "storage:object:edit"]);
        const windowOpen = vi.spyOn(window, "open").mockImplementation(() => null);
        let records = [
            {
                id: "storage-1",
                originalFilename: "sancai.png",
                contentType: "image/png",
                size: 1536,
                accessEndpoint: "/api/storage/object/storage-1/content",
                objectStatus: "ACTIVE",
                referenceStatus: "UNREFERENCED",
                referenceOwnerType: "SANCAI_ENTRY",
                priority: 100,
                remarks: "三才图会图片"
            }
        ];
        let pageRequestCount = 0;
        vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
            const url = String(input);
            if (url.endsWith("/storage/object/page")) {
                pageRequestCount += 1;
                expect(init).toEqual(
                    expect.objectContaining({
                        body: expect.any(String),
                        headers: expect.objectContaining({
                            "Access-Token": "test-token"
                        }),
                        method: "POST"
                    })
                );
                const body = JSON.parse(String(init?.body)) as Record<string, unknown>;
                expect(body).toEqual(
                    expect.objectContaining({
                        pageNo: 1,
                        pageSize: 20
                    })
                );
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                pageNo: 1,
                                pageSize: 20,
                                count: records.length,
                                records
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/storage/object/delete")) {
                expect(init).toEqual(
                    expect.objectContaining({
                        body: JSON.stringify({ ids: ["storage-1"] }),
                        headers: expect.objectContaining({
                            "Access-Token": "test-token"
                        }),
                        method: "POST"
                    })
                );
                records = records.filter((record) => record.id !== "storage-1");
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: true
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: "not found" }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        renderWithQueryClientAndApp(<StorageObjectPage />);

        expect(await screen.findByRole("heading", { name: "存储对象" })).toBeInTheDocument();
        expect(await screen.findByText("sancai.png")).toBeInTheDocument();
        expect(screen.getAllByText("1.50 KB").length).toBeGreaterThan(0);
        expect(screen.getByText("可用")).toBeInTheDocument();
        expect(screen.getByText("未引用")).toBeInTheDocument();
        expect(screen.getByText("SANCAI_ENTRY")).toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "预览 sancai.png" }));
        expect(windowOpen).toHaveBeenCalledWith(
            "/kuzhambu-admin-api/api/storage/object/storage-1/content?token=test-token",
            "_blank",
            "noopener,noreferrer"
        );

        await userEvent.click(screen.getByRole("button", { name: "下载 sancai.png" }));
        expect(windowOpen).toHaveBeenCalledWith(
            "/kuzhambu-admin-api/api/storage/object/storage-1/content?download=true&token=test-token",
            "_blank",
            "noopener,noreferrer"
        );

        fireEvent.click(screen.getByRole("button", { name: "删除 sancai.png" }));

        expect((await screen.findAllByText("删除存储对象")).length).toBeGreaterThan(0);
        expect(screen.getByText("确认删除 sancai.png？")).toBeInTheDocument();

        fireEvent.click(
            within(await screen.findByRole("dialog")).getByRole("button", { name: /删\s*除/ })
        );

        await waitFor(() => expect(screen.queryByText("sancai.png")).not.toBeInTheDocument());
        expect(pageRequestCount).toBeGreaterThanOrEqual(2);
    }, 30000);

    it("filters storage objects by reference owner", async () => {
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.permissions",
            JSON.stringify(["storage:object:view", "storage:object:edit"])
        );
        const pageRequestBodies: Array<Record<string, unknown>> = [];
        vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
            const url = String(input);
            if (url.endsWith("/storage/object/page")) {
                pageRequestBodies.push(
                    JSON.parse(String(init?.body ?? "{}")) as Record<string, unknown>
                );
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                pageNo: 1,
                                pageSize: 20,
                                count: 1,
                                records: [
                                    {
                                        id: "storage-1",
                                        originalFilename: "sancai.png",
                                        contentType: "image/png",
                                        size: 1536,
                                        accessEndpoint: "/api/storage/object/storage-1/content",
                                        objectStatus: "ACTIVE",
                                        referenceStatus: "UNREFERENCED",
                                        priority: 100,
                                        remarks: "三才图会图片"
                                    }
                                ]
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: "not found" }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        renderWithQueryClientAndApp(<StorageObjectPage />);

        expect(await screen.findByRole("heading", { name: "存储对象" })).toBeInTheDocument();
        await userEvent.click(screen.getByRole("button", { name: /筛选/ }));
        await userEvent.type(screen.getByPlaceholderText("reference_owner_type"), "BOOK");
        await userEvent.type(
            screen.getByPlaceholderText("123e4567-e89b-12d3-a456-426614174000"),
            "owner-9"
        );
        await userEvent.click(screen.getByRole("button", { name: /查\s*询/ }));

        await waitFor(() => expect(pageRequestBodies.length).toBeGreaterThan(1));
        const latestRequest = pageRequestBodies[pageRequestBodies.length - 1];
        expect(latestRequest).toMatchObject({
            pageNo: 1,
            pageSize: 20,
            referenceOwnerType: "BOOK",
            referenceOwnerId: "owner-9"
        });
        expect(latestRequest).not.toHaveProperty("ownerType");
        expect(latestRequest).not.toHaveProperty("ownerId");
    }, 30000);

    it("renders the silver user management layout interactions", async () => {
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.permissions",
            JSON.stringify(["sys:user:view", "sys:user:edit"])
        );
        replacePermissions(["sys:user:view", "sys:user:edit"]);
        vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
            const url = String(input);
            if (url.endsWith("/sys/current-user/get")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                id: "current",
                                loginName: "root",
                                name: "Root",
                                ranks: 9,
                                superAdmin: true,
                                admin: true
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }
            if (url.endsWith("/sys/user/department/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: [
                                {
                                    id: "1",
                                    name: "Product",
                                    namePath: "Product"
                                }
                            ]
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }
            if (url.endsWith("/sys/user/role/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: [
                                { id: "r1", name: "管理员" },
                                { id: "r2", name: "观察员" },
                                { id: "r3", name: "审计员" }
                            ]
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }
            if (url.endsWith("/sys/user/options/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                statusOptions: [
                                    { value: "ENABLED", label: "字典启用" },
                                    { value: "DISABLED", label: "字典禁用" }
                                ],
                                rankOptions: [
                                    { value: "0", label: "等级 0" },
                                    { value: "1", label: "等级 1" },
                                    { value: "2", label: "等级 2" },
                                    { value: "9", label: "超级管理员" }
                                ]
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }
            if (url.endsWith("/sys/user/page")) {
                const body = init?.body ? JSON.parse(String(init.body)) : {};
                const allUsers = [
                    {
                        id: "current",
                        loginName: "root",
                        name: "Root",
                        email: "root@example.com",
                        ranks: 9,
                        superAdmin: true,
                        admin: true,
                        enable: true,
                        department: { id: "1", name: "Product", namePath: "Product" },
                        roles: [{ id: "r1", name: "管理员" }]
                    },
                    {
                        id: "1",
                        loginName: "ethan",
                        name: "Ethan Chen",
                        email: "ethan@example.com",
                        ranks: 1,
                        enable: true,
                        department: { id: "1", name: "Product", namePath: "Product" },
                        roles: [{ id: "r1", name: "管理员" }]
                    },
                    {
                        id: "2",
                        loginName: "olivia",
                        name: "Olivia Martinez",
                        email: "olivia@example.com",
                        ranks: 2,
                        enable: false,
                        department: { id: "1", name: "Product", namePath: "Product" },
                        roles: [{ id: "r2", name: "观察员" }]
                    }
                ];
                const records = body.name
                    ? allUsers.filter((user) =>
                          user.name.toLowerCase().includes(String(body.name).toLowerCase())
                      )
                    : allUsers;
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                pageNo: 1,
                                pageSize: 20,
                                totalCount: records.length,
                                records
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: "not found" }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        renderWithQueryClientAndApp(<UserPage />);

        expect(screen.getByRole("heading", { name: "用户管理" })).toBeInTheDocument();
        expect(await screen.findByText("Ethan Chen")).toBeInTheDocument();
        expect((await screen.findAllByText("字典启用")).length).toBeGreaterThan(0);
        expect(screen.getByRole("switch", { name: /切换 Root 状态/ })).toBeDisabled();

        const filterButton = screen.getByRole("button", { name: /筛选/ });
        fireEvent.click(filterButton);
        fireEvent.change(screen.getByPlaceholderText("输入登录名"), {
            target: { value: "ethan" }
        });
        fireEvent.click(screen.getByRole("button", { name: /查\s*询/ }));

        expect(filterButton).toHaveAttribute("aria-expanded", "false");
        expect(screen.getByPlaceholderText("输入登录名")).toHaveValue("ethan");

        fireEvent.click(filterButton);

        fireEvent.change(screen.getByPlaceholderText("搜索用户..."), {
            target: { value: "olivia" }
        });

        expect(await screen.findByText("Olivia Martinez")).toBeInTheDocument();
        await waitFor(() => expect(screen.queryByText("Ethan Chen")).not.toBeInTheDocument());

        fireEvent.click(screen.getByRole("button", { name: "编辑 Olivia Martinez" }));

        expect(await screen.findByText("编辑用户")).toBeInTheDocument();
        expect(screen.getByDisplayValue("Olivia Martinez")).toBeInTheDocument();
        await waitFor(() =>
            expect(globalThis.fetch).toHaveBeenCalledWith(
                "/kuzhambu-admin-api/api/sys/user/role/list",
                expect.any(Object)
            )
        );
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/kuzhambu-admin-api/api/sys/user/options/list",
            expect.any(Object)
        );

        fireEvent.click(screen.getByRole("button", { name: "删除 Olivia Martinez" }));

        expect((await screen.findAllByText("删除用户")).length).toBeGreaterThan(0);
        expect(screen.getByText("确认删除 Olivia Martinez？")).toBeInTheDocument();
    }, 30000);

    it("emits sortable table row movement", () => {
        const records = [
            { id: "1", name: "Ethan Chen" },
            { id: "2", name: "Sophia Carter" }
        ];
        const onSort = vi.fn();
        const dataTransfer = {
            dropEffect: "",
            effectAllowed: "",
            setData: vi.fn()
        };

        render(
            <KuzhambuTable
                ariaLabel="可排序用户表格"
                rowKey="id"
                columns={[{ title: "用户", dataIndex: "name", key: "name", width: 160 }]}
                dataSource={records}
                onSort={onSort}
                pagination={false}
                sortable
            />
        );

        const sourceRow = screen.getByText("Ethan Chen").closest("tr");
        const targetRow = screen.getByText("Sophia Carter").closest("tr");
        expect(sourceRow).not.toBeNull();
        expect(targetRow).not.toBeNull();

        targetRow!.getBoundingClientRect = () => ({
            bottom: 40,
            height: 40,
            left: 0,
            right: 160,
            top: 0,
            width: 160,
            x: 0,
            y: 0,
            toJSON: () => undefined
        });

        fireEvent.dragStart(sourceRow!, { dataTransfer });
        fireEvent.dragOver(targetRow!, { clientY: 5, dataTransfer });
        fireEvent.drop(targetRow!, { clientY: 5, dataTransfer });

        expect(onSort).toHaveBeenCalledWith(records[0], records[1], "after");
    }, 40000);

    it("clears stale tokens when protected menu loading is unauthorized", async () => {
        localStorage.setItem("kuzhambu.admin.accessToken", "stale-token");
        vi.spyOn(globalThis, "fetch").mockResolvedValue(
            new Response(JSON.stringify({ code: "COMMON-00002", message: "未授权用户" }), {
                headers: { "Content-Type": "application/json" },
                status: 200
            })
        );

        render(<App />);

        expect(await screen.findByRole("heading", { name: "登录" })).toBeInTheDocument();
        expect(localStorage.getItem("kuzhambu.admin.accessToken")).toBeNull();
        expect(globalThis.fetch).toHaveBeenCalledWith(
            "/kuzhambu-admin-api/api/sys/current-user/get",
            expect.objectContaining({
                headers: expect.objectContaining({
                    "Access-Token": "stale-token"
                }),
                method: "POST"
            })
        );
    });

    it("refreshes the access token before expireAt is within 60 seconds", async () => {
        localStorage.setItem("kuzhambu.admin.accessToken", "expiring-token");
        localStorage.setItem("kuzhambu.admin.refreshToken", "refresh-token");
        localStorage.setItem("kuzhambu.admin.accessTokenExpireAt", String(Date.now() + 30 * 1000));
        vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
            const url = String(input);
            if (url.endsWith("/auth/session/token/refresh")) {
                expect(init).toEqual(
                    expect.objectContaining({
                        body: JSON.stringify({
                            clientId: "admin-api",
                            refreshToken: "refresh-token"
                        })
                    })
                );

                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                token: "refreshed-access-token",
                                refreshToken: "rotated-refresh-token",
                                expireAt: 1778514052155
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/get")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: {
                                id: "1",
                                loginName: "developer",
                                name: "Developer"
                            }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/menu/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({ code: "COMMON-00000", message: "success", data: [] }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            if (url.endsWith("/sys/current-user/permission/list")) {
                return Promise.resolve(
                    new Response(
                        JSON.stringify({
                            code: "COMMON-00000",
                            message: "success",
                            data: { perms: [] }
                        }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            return Promise.resolve(
                new Response(JSON.stringify({ code: "COMMON-00004", message: "not found" }), {
                    headers: { "Content-Type": "application/json" },
                    status: 404
                })
            );
        });

        render(<App />);

        expect(await screen.findByRole("heading", { name: "仪表盘" })).toBeInTheDocument();
        expect(localStorage.getItem("kuzhambu.admin.accessToken")).toBe("refreshed-access-token");
        expect(localStorage.getItem("kuzhambu.admin.refreshToken")).toBe("rotated-refresh-token");
        expect(localStorage.getItem("kuzhambu.admin.accessTokenExpireAt")).toBe("1778514052155");
        await waitFor(() =>
            expect(globalThis.fetch).toHaveBeenCalledWith(
                "/kuzhambu-admin-api/api/sys/current-user/get",
                expect.objectContaining({
                    headers: expect.objectContaining({
                        "Access-Token": "refreshed-access-token"
                    }),
                    method: "POST"
                })
            )
        );
    });

    it("waits for an in-flight token refresh before sending another request", async () => {
        localStorage.setItem("kuzhambu.admin.accessToken", "old-token");
        localStorage.setItem("kuzhambu.admin.refreshToken", "refresh-token");
        localStorage.setItem(
            "kuzhambu.admin.accessTokenExpireAt",
            String(Date.now() + 5 * 60 * 1000)
        );
        let resolveRefresh: (response: Response) => void = () => undefined;
        const refreshResponse = new Promise<Response>((resolve) => {
            resolveRefresh = resolve;
        });
        vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
            const url = String(input);
            if (url.endsWith("/sys/current-user/get")) {
                return Promise.resolve(
                    new Response(JSON.stringify({ code: "COMMON-00002", message: "未授权用户" }), {
                        headers: { "Content-Type": "application/json" },
                        status: 200
                    })
                );
            }

            if (url.endsWith("/auth/session/token/refresh")) {
                return refreshResponse;
            }

            if (url.endsWith("/sys/current-user/menu/list")) {
                expect(init).toEqual(
                    expect.objectContaining({
                        headers: expect.objectContaining({
                            "Access-Token": "new-token"
                        })
                    })
                );
                return Promise.resolve(
                    new Response(
                        JSON.stringify({ code: "COMMON-00000", message: "success", data: [] }),
                        {
                            headers: { "Content-Type": "application/json" },
                            status: 200
                        }
                    )
                );
            }

            return Promise.resolve(
                new Response(
                    JSON.stringify({ code: "COMMON-00000", message: "success", data: {} }),
                    {
                        headers: { "Content-Type": "application/json" },
                        status: 200
                    }
                )
            );
        });

        const infoRequest = getCurrentUserInfo().catch(() => null);
        await waitFor(() =>
            expect(globalThis.fetch).toHaveBeenCalledWith(
                "/kuzhambu-admin-api/api/auth/session/token/refresh",
                expect.any(Object)
            )
        );
        const menuRequest = listCurrentUserMenus();

        expect(globalThis.fetch).not.toHaveBeenCalledWith(
            "/kuzhambu-admin-api/api/sys/current-user/menu/list",
            expect.any(Object)
        );
        resolveRefresh(
            new Response(
                JSON.stringify({
                    code: "COMMON-00000",
                    message: "success",
                    data: {
                        token: "new-token",
                        refreshToken: "new-refresh-token",
                        expireAt: Date.now() + 5 * 60 * 1000
                    }
                }),
                {
                    headers: { "Content-Type": "application/json" },
                    status: 200
                }
            )
        );

        await expect(menuRequest).resolves.toEqual([]);
        await infoRequest;
    });
});
