import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
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
        expect(screen.queryByText("请选择左侧卷目查看稿件")).not.toBeInTheDocument();
    });

    it("loads task filters from batchId and contentRefs search params", async () => {
        window.history.pushState(
            {},
            "",
            `/knowledge/graph-extraction?batchId=batch-001&contentRefs=${encodeURIComponent(
                JSON.stringify([{ contentType: "SANCAI_ENTRY", contentRefId: "1001" }])
            )}`
        );

        renderPage();

        await waitFor(() => {
            expect(serviceMocks.pageTasks).toHaveBeenCalledWith({
                batchId: "batch-001",
                contentRefs: [{ contentRefId: "1001", contentType: "SANCAI_ENTRY" }],
                groupBy: "NONE",
                pageNo: 1,
                pageSize: 20
            });
        });
    });

    it("switches task list mode by requesting server grouped results", async () => {
        renderPage();

        fireEvent.click(await screen.findByText("按素材分组"));

        await waitFor(() => {
            expect(serviceMocks.pageTasks).toHaveBeenCalledWith({
                groupBy: "MATERIAL",
                pageNo: 1,
                pageSize: 20
            });
        });
        expect(await screen.findByText("任务 8010")).toBeInTheDocument();
    });

    it("sends task lock and expected state before applying candidate from detail drawer", async () => {
        renderPage();

        fireEvent.click(await screen.findByRole("button", { name: /查\s*看/u }));
        await waitFor(() => {
            expect(serviceMocks.getTask).toHaveBeenCalledWith({ taskId: "8008" });
        });

        fireEvent.click(await screen.findByText("候选处置"));
        fireEvent.click(await screen.findByRole("button", { name: "合并" }));

        await waitFor(() => {
            expect(serviceMocks.getMaterial).toHaveBeenCalledWith({
                contentRef: { contentRefId: "1001", contentType: "SANCAI_ENTRY" }
            });
            expect(serviceMocks.applyCandidate).toHaveBeenCalledWith(
                expect.objectContaining({
                    applyMode: "MERGE",
                    expectedDisposition: "PENDING",
                    expectedExecutionStatus: "SUCCEEDED",
                    materialLockVersion: "4",
                    taskId: "8008",
                    taskLockVersion: "1"
                })
            );
        });
    });

    it("refreshes detail without guessing final state when task action has conflict", async () => {
        serviceMocks.applyCandidate.mockResolvedValueOnce({
            conflict: {
                code: "GRAPH_TASK_LOCK_CONFLICT",
                message: "任务版本已变化，请刷新后重试。"
            }
        });
        renderPage();

        fireEvent.click(await screen.findByRole("button", { name: /查\s*看/u }));
        await waitFor(() => {
            expect(serviceMocks.getTask).toHaveBeenCalledWith({ taskId: "8008" });
        });

        fireEvent.click(await screen.findByText("候选处置"));
        fireEvent.click(await screen.findByRole("button", { name: "合并" }));

        await waitFor(() => {
            expect(serviceMocks.applyCandidate).toHaveBeenCalledWith(
                expect.objectContaining({
                    expectedDisposition: "PENDING",
                    expectedExecutionStatus: "SUCCEEDED",
                    taskId: "8008",
                    taskLockVersion: "1"
                })
            );
        });
        await waitFor(() => {
            expect(serviceMocks.getTask).toHaveBeenCalledTimes(2);
        });
        expect(screen.getByRole("button", { name: "合并" })).toBeInTheDocument();
    });

    it("creates batch extraction tasks from contentRefs search params", async () => {
        window.history.pushState(
            {},
            "",
            `/knowledge/graph-extraction?contentRefs=${encodeURIComponent(
                JSON.stringify([{ contentType: "SANCAI_ENTRY", contentRefId: "1001" }])
            )}`
        );
        renderPage();

        fireEvent.click(
            await screen.findByTestId("knowledge-graph-extraction-batch-create-selected-button")
        );

        await waitFor(() => {
            expect(serviceMocks.createBatchExtraction).toHaveBeenCalledWith({
                contentRefs: [{ contentRefId: "1001", contentType: "SANCAI_ENTRY" }]
            });
        });
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
            expect(serviceMocks.regenerateTask).toHaveBeenCalled();
            const regenerateCalls = serviceMocks.regenerateTask.mock.calls as unknown as Array<
                [unknown]
            >;
            expect(regenerateCalls[0]?.[0]).toEqual({
                replaceUnconfirmedOnly: true,
                selectionScopeJson: '{"sourceContentIds":[1001]}',
                sourceTaskId: "88",
                taskType: "GRAPH",
                triggerSource: "REFINEMENT_APPLIED"
            });
        });
    });

    it("ignores invalid refinement handoff source task id", async () => {
        window.history.pushState(
            {},
            "",
            "/knowledge/graph-extraction?regenerate=1&taskType=GRAPH&sourceTaskId=abc&triggerSource=REFINEMENT_APPLIED"
        );
        renderPage();

        await waitFor(() => {
            expect(screen.queryByText("精修应用后的图谱重生成参数已载入")).not.toBeInTheDocument();
        });
        expect(screen.queryByRole("button", { name: "提交重生成" })).not.toBeInTheDocument();
        expect(serviceMocks.regenerateTask).not.toHaveBeenCalled();
    });
});
