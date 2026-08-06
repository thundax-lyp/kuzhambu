import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { RolePage } from "./role-page";
import * as service from "./role-service";

vi.mock("./role-service", () => ({
    changeInfo: vi.fn(),
    changeStatus: vi.fn(),
    create: vi.fn(),
    getOptions: vi.fn(),
    list: vi.fn(),
    listMenus: vi.fn(),
    remove: vi.fn(),
    sort: vi.fn()
}));

const roles = [
    { id: "hidden-1", name: "隐藏甲", enable: true },
    { id: "visible-1", name: "运营一", enable: true },
    { id: "hidden-2", name: "隐藏乙", enable: true },
    { id: "visible-2", name: "运营二", enable: true }
];

const renderPage = () => {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } }
    });
    return render(
        <QueryClientProvider client={queryClient}>
            <App>
                <RolePage />
            </App>
        </QueryClientProvider>
    );
};

describe("RolePage", () => {
    beforeEach(() => {
        replacePermissions(["sys:role:view", "sys:role:edit"]);
        vi.mocked(service.getOptions).mockResolvedValue({
            statusOptions: [],
            privilegeOptions: []
        });
        vi.mocked(service.listMenus).mockResolvedValue([]);
    });

    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it("keeps prior roles visible and reports a failed refresh", async () => {
        vi.mocked(service.list)
            .mockResolvedValueOnce(roles)
            .mockRejectedValueOnce(new Error("角色服务暂不可用"));
        renderPage();
        await screen.findByText("运营一");

        fireEvent.click(screen.getByRole("button", { name: /刷\s*新/ }));

        expect(await screen.findByText("角色列表加载失败")).toBeInTheDocument();
        expect(
            screen.getByText("当前展示的是上次成功加载的数据，本次查询未更新。")
        ).toBeInTheDocument();
        expect(screen.getByText("运营一")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "重试加载角色列表" })).toBeInTheDocument();
    });

    it("uses filtered rows as anchors while submitting the complete global order", async () => {
        vi.mocked(service.list).mockResolvedValue(roles);
        vi.mocked(service.sort).mockResolvedValue(true);
        renderPage();
        await screen.findByText("运营一");

        fireEvent.change(screen.getByRole("textbox", { name: "搜索角色" }), {
            target: { value: "运营" }
        });
        expect(screen.queryByText("隐藏甲")).not.toBeInTheDocument();
        const sourceRow = screen.getByText("运营二").closest("tr");
        const targetRow = screen.getByText("运营一").closest("tr");
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
        const dataTransfer = {
            dropEffect: "",
            effectAllowed: "",
            setData: vi.fn()
        };

        fireEvent.dragStart(sourceRow!, { dataTransfer });
        fireEvent.dragOver(targetRow!, { clientY: 5, dataTransfer });
        fireEvent.drop(targetRow!, { clientY: 5, dataTransfer });

        await waitFor(() => expect(service.sort).toHaveBeenCalledTimes(1));
        expect(vi.mocked(service.sort).mock.calls[0][0]).toEqual({
            orderedIds: ["hidden-1", "visible-1", "visible-2", "hidden-2"],
            sortDirection: "ASC"
        });
    });
});
