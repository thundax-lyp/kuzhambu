import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { GraphMaterialPage } from "./graph-material-page";

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
});
