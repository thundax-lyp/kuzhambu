import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import * as service from "./graph-extraction-service";
import { GraphExtractionPage } from "./graph-extraction-page";

vi.mock("./graph-extraction-service", () => ({
    addTask: vi.fn(async (request) => ({
        taskId: "9001",
        taskType: request.taskType,
        status: "REQUESTED"
    })),
    applyTaskCandidate: vi.fn(async ({ taskId }) => ({
        taskId: String(taskId),
        taskType: "GRAPH",
        status: "APPLIED",
        aiCallId: 8001,
        aiCandidateId: 7001,
        appliedAt: 1710000000000
    })),
    getTaskDetail: vi.fn(async ({ taskId }) => ({
        taskId: String(taskId),
        taskType: "GRAPH",
        status: "SUCCEEDED",
        aiCallId: 8001,
        aiCandidateId: 7001,
        requestedAt: 1710000000000,
        completedAt: 1710000600000
    })),
    pageTasks: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 1,
        totalPage: 1,
        count: 1,
        records: [
            {
                taskId: "8008",
                taskType: "GRAPH",
                status: "SUCCEEDED",
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                aiCandidateId: 7001
            }
        ]
    }))
}));

describe("GraphExtractionPage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions([
            "knowledge:graph:view",
            "knowledge:graph:edit",
            "knowledge:graph:apply"
        ]);
    });

    afterEach(() => {
        vi.restoreAllMocks();
        queryClient.clear();
        cleanup();
    });

    it("renders the page and creates a graph extraction task", async () => {
        const user = userEvent.setup();
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <GraphExtractionPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(screen.getByRole("heading", { level: 2, name: "知识抽取任务" })).toBeInTheDocument();
        expect(screen.getByRole("heading", { level: 4, name: "创建抽取任务" })).toBeInTheDocument();
        expect(screen.getByRole("heading", { level: 4, name: "任务列表" })).toBeInTheDocument();
        expect(await screen.findByLabelText("知识抽取任务表格")).toBeInTheDocument();

        fireEvent.change(screen.getByLabelText("来源内容类型"), {
            target: { value: "SANCAI_ENTRY" }
        });
        fireEvent.change(screen.getByLabelText("来源内容 ID"), {
            target: { value: "1001" }
        });
        fireEvent.change(screen.getByLabelText("模型 ID"), {
            target: { value: "5001" }
        });
        fireEvent.change(screen.getByLabelText("模型名"), {
            target: { value: "gpt-5.5" }
        });
        fireEvent.change(screen.getByLabelText("Prompt Messages JSON"), {
            target: { value: '[{"role":"system","content":"extract"}]' }
        });
        fireEvent.change(screen.getByLabelText("输入 Payload JSON"), {
            target: { value: '{"content":"天地玄黄"}' }
        });
        await user.click(screen.getByRole("button", { name: "创建图谱抽取任务" }));

        await waitFor(() =>
            expect(service.addTask).toHaveBeenCalledWith(
                expect.objectContaining({
                    taskType: "GRAPH",
                    sourceContentType: "SANCAI_ENTRY",
                    sourceContentId: 1001,
                    modelId: 5001,
                    modelName: "gpt-5.5"
                })
            )
        );
        expect(await screen.findByText("最近创建任务")).toBeInTheDocument();
        expect(screen.getByText("任务号：9001")).toBeInTheDocument();
    });

});
