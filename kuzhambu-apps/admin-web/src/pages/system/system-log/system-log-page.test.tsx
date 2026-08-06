import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { SystemLogPage } from "./system-log-page";
import * as service from "./system-log-service";

vi.mock("./system-log-service", () => ({
    pageEvents: vi.fn()
}));

const logPage = {
    pageNo: 1,
    pageSize: 20,
    totalPage: 1,
    count: 1,
    totalCount: 1,
    records: [
        {
            id: "log-1",
            createDate: "2026-08-06 12:00:00",
            title: "用户登录",
            method: "POST",
            requestUri: "/api/auth/session/login",
            remoteAddr: "127.0.0.1"
        }
    ]
};

const renderPage = () => {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } }
    });
    return render(
        <QueryClientProvider client={queryClient}>
            <App>
                <SystemLogPage />
            </App>
        </QueryClientProvider>
    );
};

describe("SystemLogPage", () => {
    beforeEach(() => {
        replacePermissions(["system:log:view"]);
    });

    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it("debounces title searches before updating the query", async () => {
        vi.mocked(service.pageEvents).mockResolvedValue(logPage);
        renderPage();
        await screen.findByText("用户登录");
        expect(service.pageEvents).toHaveBeenCalledTimes(1);

        const searchInput = screen.getByRole("textbox", { name: "搜索日志" });
        fireEvent.change(searchInput, { target: { value: "用" } });
        fireEvent.change(searchInput, { target: { value: "用户" } });
        fireEvent.change(searchInput, { target: { value: "用户登录" } });

        expect(service.pageEvents).toHaveBeenCalledTimes(1);
        await waitFor(() => expect(service.pageEvents).toHaveBeenCalledTimes(2), {
            timeout: 1000
        });
        expect(service.pageEvents).toHaveBeenLastCalledWith(
            expect.objectContaining({ title: "用户登录", pageNo: 1, pageSize: 20 })
        );
    });

    it("keeps prior data visible and reports a failed refresh", async () => {
        vi.mocked(service.pageEvents)
            .mockResolvedValueOnce(logPage)
            .mockRejectedValueOnce(new Error("日志服务暂不可用"));
        renderPage();
        await screen.findByText("用户登录");

        fireEvent.click(screen.getByRole("button", { name: /刷\s*新/ }));

        expect(await screen.findByText("系统日志加载失败")).toBeInTheDocument();
        expect(
            screen.getByText("当前展示的是上次成功加载的数据，本次查询未更新。")
        ).toBeInTheDocument();
        expect(screen.getByText("用户登录")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "重试加载系统日志" })).toBeInTheDocument();
    });
});
