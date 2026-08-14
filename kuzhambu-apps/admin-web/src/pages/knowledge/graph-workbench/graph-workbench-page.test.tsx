import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { GraphWorkbenchPage } from "./graph-workbench-page";

const renderWorkbench = () => render(<GraphWorkbenchPage />);

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

        await user.type(screen.getByRole("textbox", { name: "搜索图谱种子" }), "不存在");
        expect(screen.getByText("没有匹配的图谱种子")).toBeInTheDocument();
    });

    it("renders permission and mock failure states", async () => {
        replacePermissions([]);
        const { rerender } = renderWorkbench();
        expect(screen.getByText("无权查看图谱工作台")).toBeInTheDocument();

        replacePermissions(["knowledge:graph:view"]);
        rerender(<GraphWorkbenchPage />);
        await userEvent.click(screen.getByRole("button", { name: "模拟加载失败" }));
        expect(screen.getByText("工作台 Mock 数据加载失败")).toBeInTheDocument();
    });
});
