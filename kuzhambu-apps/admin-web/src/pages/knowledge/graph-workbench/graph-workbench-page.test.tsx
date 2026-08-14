import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { GraphWorkbenchPage } from "./graph-workbench-page";

vi.mock("@/components/kuzhambu-graph", () => ({
    ["KuzhambuGraph"]: ({ spoList }: { spoList: unknown[] }) => (
        <div data-testid="knowledge-graph-workbench-canvas-mock">{spoList.length} 条关系</div>
    )
}));

const renderWorkbench = () =>
    render(
        <MemoryRouter>
            <GraphWorkbenchPage />
        </MemoryRouter>
    );

describe("GraphWorkbenchPage", () => {
    afterEach(() => {
        cleanup();
        replacePermissions([]);
    });

    it("renders metrics and filters mock seeds", async () => {
        replacePermissions(["knowledge:graph:view"]);
        const user = userEvent.setup();
        renderWorkbench();

        expect(screen.getByRole("heading", { name: "图谱工作台" })).toBeInTheDocument();
        expect(screen.getByText("覆盖素材")).toBeInTheDocument();
        expect(screen.getByText("42")).toBeInTheDocument();
        expect(screen.getByText("种子节点已淡化，等待边批次返回")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "查看节点 李白" })).toHaveAttribute(
            "data-faded",
            "true"
        );

        await waitFor(() =>
            expect(screen.getByText("边批次加载完成，已移除孤立节点")).toBeInTheDocument()
        );
        expect(screen.getByText("2 条关系")).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "查看节点 孤立节点" })).not.toBeInTheDocument();
        expect(screen.getByText("当前节点 2 / 最多 200")).toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "查看节点 李白" }));
        expect(screen.getByText("图谱节点详情")).toBeInTheDocument();
        expect(screen.getByText("唐诗素材-001")).toBeInTheDocument();

        await user.type(screen.getByRole("textbox", { name: "搜索图谱种子" }), "不存在");
        expect(screen.getByText("没有匹配的图谱种子")).toBeInTheDocument();
    });

    it("renders permission and mock failure states", async () => {
        replacePermissions([]);
        const { rerender } = renderWorkbench();
        expect(screen.getByText("无权查看图谱工作台")).toBeInTheDocument();

        replacePermissions(["knowledge:graph:view"]);
        rerender(
            <MemoryRouter>
                <GraphWorkbenchPage />
            </MemoryRouter>
        );
        await userEvent.click(screen.getByRole("button", { name: "模拟加载失败" }));
        expect(screen.getByText("工作台 Mock 数据加载失败")).toBeInTheDocument();
    });
});
