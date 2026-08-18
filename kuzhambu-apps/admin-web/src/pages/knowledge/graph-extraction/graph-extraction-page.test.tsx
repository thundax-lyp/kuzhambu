import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { clearPermissions, replacePermissions } from "@/auth/permission-storage";
import { GraphExtractionPage } from "./graph-extraction-page";

const serviceState = vi.hoisted(() => ({
    taskDisposition: "PENDING"
}));

const serviceMocks = vi.hoisted(() => ({
    applyCandidate: vi.fn(
        async (): Promise<{
            conflict?: { code: string; message: string };
            task?: { disposition?: string; status: string; taskId: string };
        }> => {
            serviceState.taskDisposition = "ADOPTED_MERGE";
            return {
                task: {
                    disposition: "ADOPTED_MERGE",
                    status: "SUCCEEDED",
                    taskId: "8008"
                }
            };
        }
    ),
    cancelTask: vi.fn(async () => ({ task: { status: "CANCELLED", taskId: "8008" } })),
    createBatchExtraction: vi.fn(async () => ({ batchId: "batch-001", materials: [] })),
    discardCandidate: vi.fn(async () => ({ task: { status: "SUCCEEDED", taskId: "8008" } })),
    getTask: vi.fn(async (request: { taskId: string }) => ({
        candidate: {
            candidateId: "7001",
            diff: [],
            edges: [],
            issues: [],
            nodes: []
        },
        materialStats: {
            activeTaskCount: "0",
            calculatedAt: "1723852820000",
            draftEdgeCount: "18",
            draftNodeCount: "12",
            failedTaskCount: "0",
            pendingReviewTaskCount: "1",
            publicationContributionCount: "0",
            publishedEdgeCount: "0",
            publishedNodeCount: "0",
            statsRevision: "4"
        },
        relatedTasks: [],
        source: {
            contentRef: {
                contentRefId: "1001",
                contentType: "SANCAI_ENTRY"
            },
            contentType: "SANCAI_ENTRY",
            title: "三才稿件"
        },
        stages: [],
        task: {
            aiCandidateId: "7001",
            attemptNo: "1",
            batchJobId: "1001",
            currentStage: "CANDIDATE_READY",
            disposition: serviceState.taskDisposition,
            executionStatus: "SUCCEEDED",
            id: request.taskId,
            lockVersion: "1",
            materialRef: {
                contentRefId: "1001",
                contentType: "SANCAI_ENTRY"
            },
            progress: 100,
            selectionScopeJson: '{"sourceContentIds":[1001,1002]}',
            status: "SUCCEEDED",
            taskId: request.taskId,
            taskType: "GRAPH",
            triggerSource: "QUALITY_REPORT"
        }
    })),
    getMaterial: vi.fn(async () => ({
        edges: [],
        material: {
            contentRef: {
                contentRefId: "1001",
                contentType: "SANCAI_ENTRY"
            },
            contentType: "SANCAI_ENTRY",
            id: "2001",
            lockVersion: "4",
            status: "DRAFT",
            title: "三才稿件"
        },
        materialStats: null,
        nodes: [],
        source: {
            contentRef: {
                contentRefId: "1001",
                contentType: "SANCAI_ENTRY"
            },
            contentType: "SANCAI_ENTRY",
            title: "三才稿件"
        },
        taskSummary: {
            activeTaskCount: "0",
            failedTaskCount: "0",
            latestTask: null,
            pendingReviewTaskCount: "0",
            totalTaskCount: "0"
        }
    })),
    pageTasks: vi.fn(async (query?: { groupBy?: string }) => ({
        count: query?.groupBy === "MATERIAL" ? 2 : 1,
        pageNo: 1,
        pageSize: 20,
        records:
            query?.groupBy === "MATERIAL"
                ? [
                      {
                          aiCandidateId: "7001",
                          attemptNo: "1",
                          batchJobId: "1001",
                          currentStage: "CANDIDATE_READY",
                          disposition: "PENDING",
                          executionStatus: "SUCCEEDED",
                          id: "8008",
                          lockVersion: "1",
                          materialRef: {
                              contentRefId: "1001",
                              contentType: "SANCAI_ENTRY"
                          },
                          progress: 100,
                          selectionScopeJson: '{"sourceContentIds":[1001,1002]}',
                          status: "SUCCEEDED",
                          taskId: "8008",
                          taskType: "GRAPH",
                          triggerSource: "QUALITY_REPORT"
                      },
                      {
                          aiCandidateId: null,
                          attemptNo: "1",
                          batchJobId: "1002",
                          currentStage: "CANDIDATE_BUILD",
                          disposition: null,
                          executionStatus: "FAILED",
                          failureReason: "候选解析失败",
                          id: "8010",
                          lockVersion: "1",
                          materialRef: {
                              contentRefId: "1002",
                              contentType: "SANCAI_ENTRY"
                          },
                          progress: 60,
                          selectionScopeJson: '{"sourceContentIds":[2001]}',
                          status: "FAILED",
                          taskId: "8010",
                          taskType: "GRAPH",
                          triggerSource: "MANUAL"
                      }
                  ]
                : [
                      {
                          aiCandidateId: "7001",
                          attemptNo: "1",
                          batchJobId: "1001",
                          currentStage: "CANDIDATE_READY",
                          disposition: "PENDING",
                          executionStatus: "SUCCEEDED",
                          id: "8008",
                          lockVersion: "1",
                          materialRef: {
                              contentRefId: "1001",
                              contentType: "SANCAI_ENTRY"
                          },
                          progress: 100,
                          selectionScopeJson: '{"sourceContentIds":[1001,1002]}',
                          status: "SUCCEEDED",
                          taskId: "8008",
                          taskType: "GRAPH",
                          triggerSource: "QUALITY_REPORT"
                      }
                  ],
        totalCount: query?.groupBy === "MATERIAL" ? 2 : 1,
        totalPage: 1
    })),
    regenerateTask: vi.fn(async () => ({ task: { status: "PENDING", taskId: "9002" } })),
    retryTask: vi.fn(async () => ({ task: { status: "PENDING", taskId: "8008" } }))
}));

vi.mock("./graph-extraction-service", () => ({
    ...serviceMocks
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
        serviceState.taskDisposition = "PENDING";
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
    });

    afterEach(() => {
        window.history.pushState({}, "", "/");
        cleanup();
    });

    it("renders the task queue as the primary page content", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "知识抽取" })).toBeInTheDocument();
        await waitFor(() => {
            expect(serviceMocks.pageTasks).toHaveBeenCalledWith({
                groupBy: "NONE",
                pageNo: 1,
                pageSize: 20
            });
        });
        expect(await screen.findByText("任务 8008")).toBeInTheDocument();
        expect(screen.getByText("素材标题")).toBeInTheDocument();
        expect(screen.getAllByText("运行状态")[0]).toBeInTheDocument();
        expect(screen.getAllByText("采纳状态")[0]).toBeInTheDocument();
        expect(screen.queryByText("请选择左侧卷目查看稿件")).not.toBeInTheDocument();
    });

    it("only exposes retry for failed tasks", async () => {
        renderPage();

        expect(screen.queryByRole("button", { name: "查看任务 8008" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "重试任务 8008" })).not.toBeInTheDocument();
    });

    it("does not load task data without graph queue permission", async () => {
        clearPermissions();

        renderPage();

        expect(await screen.findByText("无权查看知识抽取任务")).toBeInTheDocument();
        expect(serviceMocks.pageTasks).not.toHaveBeenCalled();
    });

    it("does not load task data with edit permission but without view permission", async () => {
        replacePermissions(["knowledge:graph:edit"]);

        renderPage();

        expect(await screen.findByText("无权查看知识抽取任务")).toBeInTheDocument();
        expect(serviceMocks.pageTasks).not.toHaveBeenCalled();
    });
});
