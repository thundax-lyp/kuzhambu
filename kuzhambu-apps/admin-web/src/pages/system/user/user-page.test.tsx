import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { getCurrentUserInfo } from "@/service/current-user-service";
import { UserPage } from "./user-page";
import * as service from "./user-service";

vi.mock("@/service/current-user-service", () => ({
    getCurrentUserInfo: vi.fn()
}));

vi.mock("./user-service", () => ({
    changeInfo: vi.fn(),
    changeStatus: vi.fn(),
    create: vi.fn(),
    getOptions: vi.fn(),
    listDepartments: vi.fn(),
    listRoles: vi.fn(),
    page: vi.fn(),
    remove: vi.fn(),
    uploadAvatar: vi.fn()
}));

const renderPage = () => {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } }
    });
    return {
        queryClient,
        ...render(
            <QueryClientProvider client={queryClient}>
                <App>
                    <UserPage />
                </App>
            </QueryClientProvider>
        )
    };
};

describe("UserPage", () => {
    beforeEach(() => {
        replacePermissions(["sys:user:edit"]);
        vi.mocked(getCurrentUserInfo).mockResolvedValue({
            id: "current-user",
            loginName: "root",
            name: "Root",
            ranks: 9,
            superAdmin: true
        });
        vi.mocked(service.getOptions).mockResolvedValue({
            statusOptions: [],
            rankOptions: []
        });
        vi.mocked(service.listRoles).mockResolvedValue([]);
    });

    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it("shows recoverable list and department error states", async () => {
        vi.mocked(service.page).mockRejectedValue(new Error("用户服务暂不可用"));
        vi.mocked(service.listDepartments).mockRejectedValue(new Error("部门服务暂不可用"));

        renderPage();

        expect(await screen.findByText("用户列表加载失败")).toBeInTheDocument();
        expect(screen.getByText("用户服务暂不可用")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "重试加载用户列表" })).toBeInTheDocument();
        expect(await screen.findByText("部门加载失败")).toBeInTheDocument();
        expect(screen.getByText("部门服务暂不可用")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "重试加载部门" })).toBeInTheDocument();
    });

    it("keeps cached users and departments visible when refreshes fail", async () => {
        vi.mocked(service.page)
            .mockResolvedValueOnce({
                pageNo: 1,
                pageSize: 20,
                totalPage: 1,
                count: 1,
                totalCount: 1,
                records: [
                    {
                        id: "user-1",
                        loginName: "cached-user",
                        name: "缓存用户",
                        enable: true
                    }
                ]
            })
            .mockRejectedValueOnce(new Error("用户刷新失败"));
        vi.mocked(service.listDepartments)
            .mockResolvedValueOnce([
                {
                    id: "department-1",
                    parentId: null,
                    name: "缓存部门"
                }
            ])
            .mockRejectedValueOnce(new Error("部门刷新失败"));
        renderPage();
        expect(await screen.findByText("缓存用户")).toBeInTheDocument();
        expect(await screen.findByText("缓存部门")).toBeInTheDocument();

        fireEvent.click(screen.getByTestId("system-user-user-page-actions-refresh-button"));

        expect(await screen.findByText("用户刷新失败")).toBeInTheDocument();
        expect(await screen.findByText("部门刷新失败")).toBeInTheDocument();
        expect(screen.getByText("缓存用户")).toBeInTheDocument();
        expect(screen.getByText("缓存部门")).toBeInTheDocument();
    });
});
