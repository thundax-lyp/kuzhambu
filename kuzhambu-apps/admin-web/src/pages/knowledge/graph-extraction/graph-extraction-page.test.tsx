import { render, screen } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { GraphExtractionPage } from "./graph-extraction-page";

describe("GraphExtractionPage", () => {
    it("renders the knowledge graph extraction page shell", () => {
        render(
            <AntdApp>
                <GraphExtractionPage />
            </AntdApp>
        );

        expect(screen.getByRole("heading", { level: 2, name: "知识抽取任务" })).toBeInTheDocument();
        expect(screen.getByRole("heading", { level: 4, name: "创建抽取任务" })).toBeInTheDocument();
        expect(screen.getByRole("heading", { level: 4, name: "任务列表" })).toBeInTheDocument();
        expect(screen.getByText("关系抽取")).toBeInTheDocument();
        expect(screen.getByText("图谱抽取")).toBeInTheDocument();
        expect(screen.getByText("世系抽取")).toBeInTheDocument();
        expect(screen.getByText("任务列表与详情抽屉将在下一步接入。")).toBeInTheDocument();
    });
});
