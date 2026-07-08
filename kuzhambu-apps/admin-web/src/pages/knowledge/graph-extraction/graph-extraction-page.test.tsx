import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { GraphExtractionPage } from "./graph-extraction-page";

const serviceMocks = vi.hoisted(() => ({
    addTask: vi.fn(async () => ({ taskId: "9001", taskType: "GRAPH", status: "REQUESTED" })),
    applyTaskCandidate: vi.fn(async () => ({ taskId: "9001", status: "APPLIED" })),
    cancelBatchTask: vi.fn(async () => ({ batchJobId: 1001, status: "CANCELLED" })),
    getTaskDetail: vi.fn(async () => ({
        batchJobId: 1001,
        selectionScopeJson: '{"sourceContentIds":[1001,1002]}',
        taskId: "8008",
        triggerSource: "QUALITY_REPORT",
        status: "SUCCEEDED"
    })),
    pageTasks: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 1,
        totalPage: 1,
        count: 1,
        records: [
            {
                batchJobId: 1001,
                triggerSource: "QUALITY_REPORT",
                selectionScopeJson: '{"sourceContentIds":[1001,1002]}',
                replaceUnconfirmedOnly: true,
                taskId: "8008",
                taskType: "GRAPH",
                status: "SUCCEEDED",
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                aiCandidateId: 7001
            }
        ]
    })),
    regenerateTask: vi.fn(async () => ({ taskId: "9002", taskType: "GRAPH", status: "REQUESTED" }))
}));

vi.mock("./graph-extraction-service", () => ({
    ...serviceMocks
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
        window.history.pushState({}, "", "/");
        queryClient.clear();
        cleanup();
    });

    it("renders page shell", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <GraphExtractionPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "知识抽取任务" })).toBeInTheDocument();
        expect(await screen.findByText("8008")).toBeInTheDocument();
        expect(screen.getAllByText("1001").length).toBeGreaterThan(0);
        expect(screen.getByText("QUALITY_REPORT")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: /查\s*看/u }));

        expect(await screen.findByText('{"sourceContentIds":[1001,1002]}')).toBeInTheDocument();
    }, 45000);

    it("submits refinement handoff regenerate payload from search params", async () => {
        window.history.pushState(
            {},
            "",
            "/knowledge/graph-extraction?regenerate=1&taskType=GRAPH&sourceTaskId=88&triggerSource=REFINEMENT_APPLIED&replaceUnconfirmedOnly=true&selectionScopeJson=%7B%22sourceContentIds%22%3A%5B1001%5D%7D"
        );
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <GraphExtractionPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByText("精修应用后的图谱重生成参数已载入")).toBeInTheDocument();
        expect(screen.getByDisplayValue("88")).toBeInTheDocument();
        expect(screen.getByDisplayValue("REFINEMENT_APPLIED")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: "提交精修重生成" }));

        await waitFor(() => {
            expect(serviceMocks.regenerateTask).toHaveBeenCalledWith({
                taskType: "GRAPH",
                sourceTaskId: 88,
                triggerSource: "REFINEMENT_APPLIED",
                replaceUnconfirmedOnly: true,
                selectionScopeJson: '{"sourceContentIds":[1001]}'
            });
        });
    }, 30000);
});
