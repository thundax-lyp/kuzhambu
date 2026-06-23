import { cleanup, render, screen } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { replacePermissions } from "@/auth/permission-storage";
import { GraphResultsPage } from "./graph-results-page";

describe("GraphResultsPage", () => {
    beforeEach(() => {
        replacePermissions(["knowledge:graph:view"]);
    });

    afterEach(() => {
        cleanup();
    });

    it("renders the standalone graph results shell", async () => {
        render(
            <AntdApp>
                <GraphResultsPage />
            </AntdApp>
        );

        expect(screen.getByRole("heading", { level: 2, name: "正式结果读取" })).toBeInTheDocument();
        expect(screen.getByRole("heading", { level: 4, name: "结果入口" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "图谱版本" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "正式实体" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "正式关系" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "正式世系" })).toBeInTheDocument();
        expect(
            await screen.findByText("图谱版本列表即将接入，支持按任务类型、状态和来源内容筛选。")
        ).toBeInTheDocument();
    });
});
