import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { GraphDeletionChangePage } from "./graph-deletion-change-page";

describe("GraphDeletionChangePage", () => {
    afterEach(() => {
        cleanup();
        replacePermissions([]);
    });

    it("shows deletion impact and both irreversible decisions", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:apply"]);
        render(<GraphDeletionChangePage />);
        const user = userEvent.setup();
        expect(screen.getByRole("heading", { name: "图谱删除变更" })).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: "查看影响" }));
        expect(screen.getByText("以下操作不可逆，请确认影响后选择。")).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: "撤回关联" }));
        expect(screen.getByText("已选择 WITHDRAW_ASSOCIATIONS")).toBeInTheDocument();
    });

    it("shows permission, empty and failure states", async () => {
        replacePermissions([]);
        const { rerender } = render(<GraphDeletionChangePage />);
        expect(screen.getByText("无权查看图谱删除变更")).toBeInTheDocument();
        replacePermissions(["knowledge:graph:view"]);
        rerender(<GraphDeletionChangePage />);
        const user = userEvent.setup();
        await user.click(screen.getByRole("button", { name: "模拟空态" }));
        expect(screen.getByText("暂无删除变更")).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: "模拟加载失败" }));
        expect(screen.getByText("删除变更预检失败，请稍后重试。")).toBeInTheDocument();
    });
});
