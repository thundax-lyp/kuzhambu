import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { RoleRecord } from "@/pages/system/role/role-types";
import { RoleEditDrawer } from "./role-edit-drawer";
import * as service from "@/pages/system/role/role-service";

vi.mock("@/pages/system/role/role-service", () => ({
    changeInfo: vi.fn(),
    create: vi.fn(),
    listMenus: vi.fn()
}));

const role: RoleRecord = {
    id: "role-1",
    name: "运营管理员",
    admin: false,
    enable: true,
    menus: [{ id: "menu-1", name: "内容管理" }]
};

const renderDrawer = (onClose = vi.fn()) => {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } }
    });
    return {
        onClose,
        ...render(
            <QueryClientProvider client={queryClient}>
                <App>
                    <RoleEditDrawer open role={role} onClose={onClose} />
                </App>
            </QueryClientProvider>
        )
    };
};

describe("RoleEditDrawer", () => {
    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it("owns the edit mutation and closes after saving", async () => {
        vi.mocked(service.listMenus).mockResolvedValue([{ id: "menu-1", name: "内容管理" }]);
        vi.mocked(service.changeInfo).mockResolvedValue(role);
        const { onClose } = renderDrawer();

        await screen.findByText("内容管理");
        fireEvent.click(screen.getByRole("button", { name: /保\s*存/ }));

        await waitFor(() => expect(service.changeInfo).toHaveBeenCalledTimes(1));
        expect(service.changeInfo).toHaveBeenCalledWith(
            expect.objectContaining({ id: "role-1", name: "运营管理员" })
        );
        await waitFor(() => expect(onClose).toHaveBeenCalledTimes(1));
    });

    it("blocks saving and offers retry when menu permissions fail", async () => {
        vi.mocked(service.listMenus).mockRejectedValue(new Error("菜单服务暂不可用"));
        renderDrawer();

        expect(await screen.findByText("菜单权限加载失败")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "重试加载菜单权限" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /保\s*存/ })).toBeDisabled();
    });
});
