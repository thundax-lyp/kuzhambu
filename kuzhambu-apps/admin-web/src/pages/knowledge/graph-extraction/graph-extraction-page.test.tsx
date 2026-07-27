import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { replacePermissions } from "@/auth/permission-storage";
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

const workbenchServiceMocks = vi.hoisted(() => ({
    applyCandidate: vi.fn(async () => ({
        taskId: 9001,
        graphVersionId: 8001,
        graphStatus: "APPLIED"
    })),
    extractManuscript: vi.fn(async (request: unknown) => {
        void request;
        return {
            taskId: "9001",
            taskType: "GRAPH",
            status: "REQUESTED"
        };
    }),
    getLatestCandidate: vi.fn(async () => ({
        taskId: 9001,
        aiCandidateId: 7001,
        taskType: "GRAPH",
        status: "SUCCEEDED",
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: 1001,
        candidatePayloadJson: '{"entities":[{"name":"黄帝"}]}'
    })),
    getManuscript: vi.fn(async () => ({
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: 1001,
        title: "三才稿件",
        summary: "三才稿件摘要",
        sourcePath: "人物 / 三才稿件",
        currentVersionNo: 2,
        graphStatus: "CANDIDATE_READY",
        latestExtractionTask: {
            taskId: "9001",
            taskType: "GRAPH",
            status: "SUCCEEDED",
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: 1001,
            aiCandidateId: 7001
        },
        latestGraphVersion: {
            versionId: 8001,
            taskId: "9001",
            graphStatus: "APPLIED"
        }
    })),
    listManuscriptTree: vi.fn(async () => [
        {
            nodeKey: "MANUSCRIPT:SANCAI_ENTRY:1001",
            nodeType: "MANUSCRIPT",
            title: "三才稿件",
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: 1001,
            graphStatus: "CANDIDATE_READY"
        },
        {
            nodeKey: "MANUSCRIPT:WANGQI_DOCUMENT:2001",
            nodeType: "MANUSCRIPT",
            title: "王圻稿件",
            sourceContentType: "WANGQI_DOCUMENT",
            sourceContentId: 2001,
            graphStatus: "NOT_EXTRACTED"
        },
        {
            nodeKey: "MANUSCRIPT:MING_CUSTOMS:3001",
            nodeType: "MANUSCRIPT",
            title: "明俗稿件",
            sourceContentType: "MING_CUSTOMS",
            sourceContentId: 3001,
            graphStatus: "NOT_EXTRACTED"
        }
    ])
}));

vi.mock("./graph-extraction-service", () => ({
    ...serviceMocks
}));

vi.mock("./graph-workbench-service", () => ({
    ...workbenchServiceMocks
}));

vi.mock("@/service/current-user-service", () => ({
    getCurrentUserInfo: vi.fn(async () => ({
        id: "99",
        loginName: "admin"
    }))
}));

const createTestQueryClient = () =>
    new QueryClient({
        defaultOptions: {
            queries: {
                retry: false
            }
        }
    });

const renderPage = () => {
    const testQueryClient = createTestQueryClient();
    return render(
        <QueryClientProvider client={testQueryClient}>
            <AntdApp>
                <GraphExtractionPage />
            </AntdApp>
        </QueryClientProvider>
    );
};

describe("GraphExtractionPage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        replacePermissions([
            "knowledge:graph:view",
            "knowledge:graph:edit",
            "knowledge:graph:apply"
        ]);
    });

    afterEach(() => {
        vi.restoreAllMocks();
        window.history.pushState({}, "", "/");
        cleanup();
    });

    it("renders page shell", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "知识抽取任务" })).toBeInTheDocument();
        await waitFor(() => {
            expect(serviceMocks.pageTasks).toHaveBeenCalledWith({
                pageNo: 1,
                pageSize: 20
            });
        });
        await waitFor(() => {
            expect(workbenchServiceMocks.listManuscriptTree).toHaveBeenCalledWith();
        });
        expect(await screen.findByText("8008")).toBeInTheDocument();
        expect(await screen.findByText("三才稿件")).toBeInTheDocument();
        expect(screen.getAllByText("1001").length).toBeGreaterThan(0);
        expect(screen.getByText("QUALITY_REPORT")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: /查\s*看/u }));

        await waitFor(() => {
            expect(serviceMocks.getTaskDetail).toHaveBeenCalledWith({ taskId: 8008 });
        });
        expect(await screen.findByText('{"sourceContentIds":[1001,1002]}')).toBeInTheDocument();
    }, 60_000);

    it("runs manuscript workbench flow with automatic extraction and candidate apply", async () => {
        renderPage();

        fireEvent.click(await screen.findByText("三才稿件"));

        await waitFor(() => {
            expect(workbenchServiceMocks.getManuscript).toHaveBeenCalledWith({
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001
            });
        });
        await waitFor(() => {
            expect(workbenchServiceMocks.getLatestCandidate).toHaveBeenCalledWith({
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                taskType: "GRAPH"
            });
        });

        fireEvent.click(screen.getByRole("button", { name: "抽取图谱" }));
        await waitFor(() => {
            expect(workbenchServiceMocks.extractManuscript).toHaveBeenCalledWith({
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                taskType: "GRAPH",
                requestedBy: 99
            });
        });
        const extractCommand = workbenchServiceMocks.extractManuscript.mock.calls[0]?.[0];
        expect(extractCommand).not.toHaveProperty("inputPayloadJson");
        expect(extractCommand).not.toHaveProperty("promptMessagesJson");

        fireEvent.click(screen.getByRole("button", { name: "应用候选" }));
        await waitFor(() => {
            expect(workbenchServiceMocks.applyCandidate).toHaveBeenCalledWith({ taskId: 9001 });
        });
    });

    it("does not load workbench data without graph view permission", async () => {
        replacePermissions([]);

        renderPage();

        await waitFor(() => {
            expect(workbenchServiceMocks.listManuscriptTree).not.toHaveBeenCalled();
            expect(serviceMocks.pageTasks).not.toHaveBeenCalled();
        });
    });

    it("submits refinement handoff regenerate payload from search params", async () => {
        window.history.pushState(
            {},
            "",
            "/knowledge/graph-extraction?regenerate=1&taskType=GRAPH&sourceTaskId=88&triggerSource=REFINEMENT_APPLIED&replaceUnconfirmedOnly=true&selectionScopeJson=%7B%22sourceContentIds%22%3A%5B1001%5D%7D"
        );
        renderPage();

        expect(await screen.findByText("精修应用后的图谱重生成参数已载入")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: "提交重生成" }));

        await waitFor(() => {
            const regenerateCalls = serviceMocks.regenerateTask.mock.calls as unknown as Array<
                [unknown]
            >;
            expect(regenerateCalls[0]?.[0]).toEqual({
                taskType: "GRAPH",
                sourceTaskId: 88,
                triggerSource: "REFINEMENT_APPLIED",
                replaceUnconfirmedOnly: true,
                selectionScopeJson: '{"sourceContentIds":[1001]}'
            });
        });
    });
});
