import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { GraphGovernancePage } from "./graph-governance-page";

vi.mock("@/components/kuzhambu-graph", () => ({
    KuzhambuGraph: () => <div data-testid="knowledge-graph-governance-canvas-mock" />
}));

describe("GraphGovernancePage", () => {
    afterEach(() => {
        cleanup();
        replacePermissions([]);
    });

    it("shows nodes, relations, local canvas and detail", async () => {
        replacePermissions(["knowledge:graph:view"]);
        render(<GraphGovernancePage />);
        const user = userEvent.setup();

        expect(screen.getByRole("heading", { name: "图谱治理" })).toBeInTheDocument();
        expect(screen.getByText("李白")).toBeInTheDocument();
        expect(screen.getByText("创作")).toBeInTheDocument();
        await user.click(screen.getAllByRole("button", { name: "查看节点" })[0]);
        expect(screen.getByText("治理来源与审计详情")).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: "查看关系" }));
        expect(screen.getByText("正式图谱关系")).toBeInTheDocument();
        expect(screen.getByText("当前仅浏览，不提供高风险写操作。")).toBeInTheDocument();
    });

    it("shows permission, empty and failure states", async () => {
        replacePermissions([]);
        const { rerender } = render(<GraphGovernancePage />);
        expect(screen.getByText("无权查看图谱治理")).toBeInTheDocument();

        replacePermissions(["knowledge:graph:view"]);
        rerender(<GraphGovernancePage />);
        const user = userEvent.setup();
        await user.click(screen.getByRole("button", { name: "模拟空态" }));
        expect(screen.getByText("暂无治理节点或关系")).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: "模拟加载失败" }));
        expect(screen.getByText("治理记录加载失败，请重试。")).toBeInTheDocument();
    });
});
