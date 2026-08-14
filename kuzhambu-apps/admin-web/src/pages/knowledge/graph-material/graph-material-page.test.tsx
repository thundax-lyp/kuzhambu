import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { GraphMaterialPage } from "./graph-material-page";

vi.mock("@/components/kuzhambu-graph", () => ({
    ["KuzhambuGraph"]: () => <div data-testid="knowledge-graph-material-canvas-mock" />
}));

describe("GraphMaterialPage", () => {
    afterEach(() => {
        cleanup();
        replacePermissions([]);
    });

    it("shows five material states and failure reason", () => {
        replacePermissions(["knowledge:graph:view"]);
        render(<GraphMaterialPage />);

        expect(screen.getByRole("heading", { name: "图谱素材库" })).toBeInTheDocument();
        expect(screen.getByText("草稿")).toBeInTheDocument();
        expect(screen.getByText("发布中")).toBeInTheDocument();
        expect(screen.getByText("已发布")).toBeInTheDocument();
        expect(screen.getByText("撤回中")).toBeInTheDocument();
        expect(screen.getByText("失败")).toBeInTheDocument();
        expect(screen.getByText("抽取任务在校验实体名称时失败。")).toBeInTheDocument();
        expect(screen.getAllByRole("button", { name: "发起抽取任务" })).toHaveLength(2);
    });

    it("does not offer draft writes for publishing or withdrawing materials", () => {
        replacePermissions(["knowledge:graph:view"]);
        render(<GraphMaterialPage />);

        expect(
            screen.queryByTestId("knowledge-graph-material-extract-material-publishing-button")
        ).not.toBeInTheDocument();
        expect(
            screen.queryByTestId("knowledge-graph-material-extract-material-withdrawing-button")
        ).not.toBeInTheDocument();
    });

    it("shows permission, empty and failure states", async () => {
        replacePermissions([]);
        const { rerender } = render(<GraphMaterialPage />);
        expect(screen.getByText("无权查看图谱素材库")).toBeInTheDocument();

        replacePermissions(["knowledge:graph:view"]);
        rerender(<GraphMaterialPage />);
        const user = userEvent.setup();
        await user.click(screen.getByRole("button", { name: "模拟空态" }));
        expect(screen.getByText("暂无图谱素材")).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: "模拟加载失败" }));
        expect(screen.getByText("素材库 Mock 数据加载失败")).toBeInTheDocument();
    });

    it("keeps selected order and preserves partial publication failures", async () => {
        replacePermissions(["knowledge:graph:view"]);
        render(<GraphMaterialPage />);
        const user = userEvent.setup();

        await user.click(screen.getByRole("button", { name: "选择 唐诗选注" }));
        await user.click(screen.getByRole("button", { name: "选择 古诗源" }));
        expect(screen.getByText("批量发布（2）")).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: "批量发布（2）" }));

        await user.click(screen.getByRole("button", { name: "确认批量发布" }));
        const panel = screen.getByTestId("knowledge-graph-material-batch-panel");
        expect(panel.textContent).toContain("唐诗选注");
        expect(panel.textContent).toContain("古诗源");
        expect(screen.getByText("部分素材发布失败，其余结果已保留。")).toBeInTheDocument();
        expect(screen.getByText("发布预览存在未解决冲突。")).toBeInTheDocument();
    });

    it("shows draft editing controls and published read-only result", async () => {
        replacePermissions(["knowledge:graph:view"]);
        render(<GraphMaterialPage />);
        const user = userEvent.setup();

        await user.click(screen.getByTestId("knowledge-graph-material-open-material-draft-button"));
        expect(screen.getByText("素材画布：唐诗选注")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "新增对象" })).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: "对象：李白" }));
        expect(screen.getByText("素材对象详情")).toBeInTheDocument();

        await user.click(screen.getByTestId("knowledge-graph-material-close-draft-canvas-button"));
        await user.click(
            screen.getByTestId("knowledge-graph-material-open-material-published-button")
        );
        expect(screen.getByText("发布结果：已成功发布")).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "新增对象" })).not.toBeInTheDocument();
        expect(screen.getByRole("button", { name: "撤回素材" })).toBeInTheDocument();
    });

    it("keeps publication disabled until red conflicts are resolved, then freezes and withdraws", async () => {
        replacePermissions(["knowledge:graph:view"]);
        render(<GraphMaterialPage />);
        const user = userEvent.setup();
        await user.click(screen.getByTestId("knowledge-graph-material-open-material-draft-button"));

        expect(screen.getByText("green")).toBeInTheDocument();
        expect(screen.getByText("orange")).toBeInTheDocument();
        expect(screen.getByText("red")).toBeInTheDocument();
        expect(screen.getByText("blue")).toBeInTheDocument();
        expect(
            screen.getByTestId("knowledge-graph-material-publish-preview-button")
        ).toBeDisabled();

        await user.click(screen.getByRole("button", { name: "标记冲突已解决" }));
        await user.click(screen.getByTestId("knowledge-graph-material-publish-preview-button"));
        expect(screen.getByText("发布已冻结")).toBeInTheDocument();
        await user.click(screen.getByTestId("knowledge-graph-material-withdraw-preview-button"));
        expect(screen.getByText("素材已撤回")).toBeInTheDocument();
    });
});
