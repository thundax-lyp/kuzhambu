import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import { afterEach, describe, expect, it, vi } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { graphMaterialMockDetails } from "@/pages/knowledge/graph-material/__mocks__/graph-mock-data";
import * as service from "@/pages/knowledge/graph-material/graph-material-service";
import type { GraphMaterialDetailRecord } from "@/pages/knowledge/graph-material/graph-material-types";
import { MaterialTaskSummarySection } from "./material-task-summary-section";

vi.mock("@/pages/knowledge/graph-material/graph-material-service", () => ({
    createExtraction: vi.fn(),
    applyCandidate: vi.fn()
}));

vi.mock("@/components/kuzhambu-graph", () => ({
    ["KuzhambuGraph"]: ({ spoList }: { spoList: Array<{ predicate: string }> }) => (
        <div data-testid="knowledge-graph-material-candidate-canvas-mock">
            {spoList.length} 条关系：{spoList.map((item) => item.predicate).join("、")}
        </div>
    )
}));

const renderPanel = (detail: GraphMaterialDetailRecord | null) => {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } }
    });
    render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <MaterialTaskSummarySection detail={detail} />
            </AntdApp>
        </QueryClientProvider>
    );
};

describe("MaterialTaskSummarySection", () => {
    afterEach(() => {
        replacePermissions([]);
        vi.clearAllMocks();
    });

    it("renders task summary without draft editing controls", () => {
        renderPanel(graphMaterialMockDetails[3]);

        expect(screen.getByTestId("knowledge-graph-material-detail-tasks-section")).toBeVisible();
        expect(screen.getByText("任务摘要")).toBeInTheDocument();
        expect(screen.getByText("运行中任务")).toBeInTheDocument();
        expect(screen.getByText("最近任务")).toBeInTheDocument();
        expect(screen.getByText("抽取结果预览")).toBeInTheDocument();
        expect(screen.getByText("抽取节点")).toBeInTheDocument();
        expect(screen.getByText("抽取边")).toBeInTheDocument();
        expect(
            screen.getByTestId("knowledge-graph-material-candidate-canvas-mock")
        ).toHaveTextContent("1 条关系：提及");
        expect(screen.getByText("7002")).toBeInTheDocument();
        expect(screen.getByText("运行中")).toBeInTheDocument();
        expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
        expect(screen.getByTestId("knowledge-graph-material-detail-extract-button")).toHaveClass(
            "ant-btn-primary"
        );
        expect(screen.getByTestId("knowledge-graph-material-detail-extract-button")).toBeDisabled();
        expect(screen.queryByRole("button", { name: "新增对象" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "抽取草稿" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "导入草稿" })).not.toBeInTheDocument();
    });

    it("creates extraction task with material content reference", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.createExtraction).mockResolvedValue({
            attemptNo: "1",
            currentStage: "已提交",
            disposition: "PENDING",
            executionStatus: "PENDING",
            id: "7101",
            lockVersion: "1",
            materialRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
            progress: 0
        });
        renderPanel(graphMaterialMockDetails[1]);

        fireEvent.click(screen.getByRole("button", { name: /抽取/u }));

        await waitFor(() => {
            expect(vi.mocked(service.createExtraction).mock.calls[0]?.[0]).toEqual({
                contentRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" }
            });
        });
        expect(await screen.findByText("抽取任务已创建 #7101")).toBeInTheDocument();
    });

    it("merges a completed pending candidate into the material graph", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.applyCandidate).mockResolvedValue({});
        const task = {
            ...graphMaterialMockDetails[1].taskSummary!.latestTask!,
            disposition: "PENDING" as const,
            executionStatus: "SUCCEEDED" as const
        };
        const detail = {
            ...graphMaterialMockDetails[1],
            extractionTasks: [task],
            taskSummary: {
                ...graphMaterialMockDetails[1].taskSummary!,
                latestTask: task
            }
        };
        renderPanel(detail);

        fireEvent.click(
            screen.getByTestId("knowledge-graph-material-detail-merge-candidate-button")
        );

        await waitFor(() => {
            expect(service.applyCandidate).toHaveBeenCalledWith({
                applyMode: "MERGE",
                materialLockVersion: detail.material!.lockVersion,
                taskId: task.id,
                taskLockVersion: task.lockVersion
            });
        });
        expect(
            screen.getByTestId("knowledge-graph-material-detail-replace-candidate-button")
        ).toBeEnabled();
        expect(await screen.findByText("抽取结果已合并到知识图谱")).toBeInTheDocument();
    });
});
