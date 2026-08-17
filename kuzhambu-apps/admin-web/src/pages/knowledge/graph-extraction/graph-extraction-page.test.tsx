import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { clearPermissions, replacePermissions } from "@/auth/permission-storage";
import { GraphExtractionPage } from "./graph-extraction-page";

vi.mock("@/components/kuzhambu-graph", () =>
    Object.fromEntries([["KuzhambuGraph", () => <div aria-label="当前图谱关系图" role="img" />]])
);

const serviceMocks = vi.hoisted(() => ({
    addTask: vi.fn(async () => ({ taskId: "9001", taskType: "GRAPH", status: "REQUESTED" })),
    applyTaskCandidate: vi.fn(async () => ({ taskId: "9001", status: "APPLIED" })),
    cancelBatchTask: vi.fn(async () => ({ batchJobId: "1001", status: "CANCELLED" })),
    getTaskDetail: vi.fn(async (request: { taskId: string }) =>
        request.taskId === "9001"
            ? {
                  aiCandidateId: "7001",
                  sourceContentId: "1001",
                  sourceContentType: "SANCAI_ENTRY",
                  status: "SUCCEEDED",
                  taskId: "9001",
                  taskType: "GRAPH"
              }
            : {
                  batchJobId: "1001",
                  selectionScopeJson: '{"sourceContentIds":[1001,1002]}',
                  aiCandidateId: "7001",
                  taskId: "8008",
                  triggerSource: "QUALITY_REPORT",
                  status: "SUCCEEDED"
              }
    ),
    pageTasks: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 1,
        totalPage: 1,
        count: 1,
        records: [
            {
                batchJobId: "1001",
                triggerSource: "QUALITY_REPORT",
                selectionScopeJson: '{"sourceContentIds":[1001,1002]}',
                replaceUnconfirmedOnly: true,
                taskId: "8008",
                taskType: "GRAPH",
                status: "SUCCEEDED",
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: "1001",
                aiCandidateId: "7001"
            }
        ]
    })),
    regenerateTask: vi.fn(async () => ({ taskId: "9002", taskType: "GRAPH", status: "REQUESTED" }))
}));

const workbenchServiceMocks = vi.hoisted(() => ({
    applyCandidate: vi.fn(async () => ({
        taskId: "9001",
        graphVersionId: "8001",
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
        taskId: "9001",
        aiCandidateId: "7001",
        taskType: "GRAPH",
        status: "SUCCEEDED",
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: "1001",
        candidatePayloadJson:
            '{"entities":[{"name":"黄帝"},{"name":"制度"}],"relations":[{"sourceName":"黄帝","relationType":"建立","targetName":"制度"}]}'
    })),
    getManuscript: vi.fn(async () => ({
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: "1001",
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
            sourceContentId: "1001",
            aiCandidateId: "7001"
        },
        latestGraphVersion: {
            versionId: "8001",
            taskId: "9001",
            graphStatus: "APPLIED"
        }
    })),
    listManuscriptTree: vi.fn(async (request?: { parentKey?: string | null }) => {
        switch (request?.parentKey) {
            case "SOURCE_ROOT:SANCAI_ENTRY":
                return [
                    {
                        nodeKey: "CATEGORY:SANCAI_ENTRY:people",
                        parentKey: "SOURCE_ROOT:SANCAI_ENTRY",
                        nodeType: "CATEGORY",
                        title: "人物",
                        sourceContentType: "SANCAI_ENTRY",
                        children: [
                            {
                                nodeKey: "VOLUME:SANCAI_ENTRY:people:10",
                                parentKey: "CATEGORY:SANCAI_ENTRY:people",
                                nodeType: "VOLUME",
                                title: "卷一",
                                sourceContentType: "SANCAI_ENTRY",
                                sourceContentId: "10"
                            }
                        ]
                    }
                ];
            case "CATEGORY:SANCAI_ENTRY:people":
                return [
                    {
                        nodeKey: "VOLUME:SANCAI_ENTRY:people:10",
                        parentKey: "CATEGORY:SANCAI_ENTRY:people",
                        nodeType: "VOLUME",
                        title: "卷一",
                        sourceContentType: "SANCAI_ENTRY",
                        sourceContentId: "10"
                    }
                ];
            case "VOLUME:SANCAI_ENTRY:people:10":
                return [
                    {
                        nodeKey: "MANUSCRIPT:SANCAI_ENTRY:1001",
                        parentKey: "VOLUME:SANCAI_ENTRY:people:10",
                        nodeType: "MANUSCRIPT",
                        title: "三才稿件",
                        sourceContentType: "SANCAI_ENTRY",
                        sourceContentId: "1001",
                        graphStatus: "CANDIDATE_READY"
                    }
                ];
            default:
                return [
                    {
                        nodeKey: "SOURCE_ROOT:SANCAI_ENTRY",
                        nodeType: "SOURCE_ROOT",
                        title: "三才",
                        children: [
                            {
                                nodeKey: "CATEGORY:SANCAI_ENTRY:people",
                                parentKey: "SOURCE_ROOT:SANCAI_ENTRY",
                                nodeType: "CATEGORY",
                                title: "人物",
                                sourceContentType: "SANCAI_ENTRY",
                                children: [
                                    {
                                        nodeKey: "VOLUME:SANCAI_ENTRY:people:10",
                                        parentKey: "CATEGORY:SANCAI_ENTRY:people",
                                        nodeType: "VOLUME",
                                        title: "卷一",
                                        sourceContentType: "SANCAI_ENTRY",
                                        sourceContentId: "10"
                                    }
                                ]
                            }
                        ]
                    }
                ];
        }
    })
}));

vi.mock("./graph-extraction-service", () => ({
    ...serviceMocks
}));

vi.mock("./graph-workbench-service", () => ({
    ...workbenchServiceMocks
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

const selectManuscriptTreeNode = async (title: string) => {
    await waitFor(() => expect(document.querySelector(".ant-tree")).toBeInTheDocument());
    const tree = document.querySelector(".ant-tree") as HTMLElement;
    const titleElements = await within(tree).findAllByText(title);
    const treeTitleElement = titleElements[0];

    fireEvent.click(treeTitleElement.closest(".ant-tree-node-content-wrapper") || treeTitleElement);
};

const selectVolumeAndManuscript = async () => {
    await selectManuscriptTreeNode("人物");
    await selectManuscriptTreeNode("卷一");
    const titleElements = await screen.findAllByText("三才稿件");
    const tableTitleElement = titleElements.find((element) => element.closest(".ant-table"));
    expect(tableTitleElement).toBeDefined();
    if (!tableTitleElement) {
        return;
    }
    const row = tableTitleElement.closest("tr") as HTMLElement;
    const viewButton = within(row).getByRole("button", { name: /查\s*看/u });
    fireEvent.click(viewButton);
};

describe("GraphExtractionPage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.useRealTimers();
        replacePermissions([
            "knowledge:graph:view",
            "knowledge:graph:edit",
            "knowledge:graph:apply",
            "knowledge:refinement:edit"
        ]);
    });

    afterEach(() => {
        vi.restoreAllMocks();
        window.history.pushState({}, "", "/");
        cleanup();
    });

    it("renders page shell", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "知识抽取" })).toBeInTheDocument();
        await waitFor(() => {
            expect(serviceMocks.pageTasks).toHaveBeenCalledWith({
                pageNo: 1,
                pageSize: 20
            });
        });
        await waitFor(() => {
            expect(workbenchServiceMocks.listManuscriptTree).toHaveBeenCalledWith({});
        });
        expect(await screen.findByText("人物")).toBeInTheDocument();
        expect(screen.queryByText("王圻")).not.toBeInTheDocument();
        expect(screen.queryByText("明俗")).not.toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: "任务列表(1)" }));

        expect(await screen.findByText("8008")).toBeInTheDocument();
        expect(screen.getByText("QUALITY_REPORT")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: /查\s*看/u }));

        await waitFor(() => {
            expect(serviceMocks.getTaskDetail).toHaveBeenCalledWith({ taskId: "8008" });
        });
        expect(await screen.findByText('{"sourceContentIds":[1001,1002]}')).toBeInTheDocument();
    }, 60_000);

    it("runs manuscript workbench flow with automatic extraction and candidate apply", async () => {
        renderPage();

        await selectVolumeAndManuscript();

        await waitFor(() => {
            expect(workbenchServiceMocks.getManuscript).toHaveBeenCalledWith({
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: "1001"
            });
        });
        await waitFor(() => {
            expect(workbenchServiceMocks.getLatestCandidate).toHaveBeenCalledWith({
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: "1001",
                taskType: "GRAPH"
            });
        });
        expect(await screen.findByText("当前图谱 #8001")).toBeInTheDocument();
        expect(screen.getByText("暂无当前图谱")).toBeInTheDocument();
        expect(screen.queryByRole("table", { name: "当前图谱 SPO 列表" })).not.toBeInTheDocument();
        expect(screen.queryByRole("img", { name: "当前图谱关系图" })).not.toBeInTheDocument();
        expect(screen.queryByText("7001")).not.toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: "抽取图谱" }));
        expect(await screen.findByText("图谱抽取")).toBeInTheDocument();
        expect(await screen.findByRole("table", { name: "候选 SPO 列表" })).toBeInTheDocument();
        expect(workbenchServiceMocks.extractManuscript).not.toHaveBeenCalled();
        const regenerateButton = screen.getByTestId(
            "knowledge-graph-extraction-candidate-extract-button"
        );
        await waitFor(() => expect(regenerateButton).toBeEnabled());
        fireEvent.click(regenerateButton);
        await waitFor(() => {
            expect(workbenchServiceMocks.extractManuscript).toHaveBeenCalledWith({
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: "1001",
                taskType: "GRAPH"
            });
        });
        const extractCommand = workbenchServiceMocks.extractManuscript.mock.calls[0]?.[0];
        expect(extractCommand).not.toHaveProperty("inputPayloadJson");
        expect(extractCommand).not.toHaveProperty("promptMessagesJson");

        expect(screen.getByRole("table", { name: "候选 SPO 列表" })).toBeInTheDocument();
        await waitFor(() => {
            expect(serviceMocks.getTaskDetail).toHaveBeenCalledWith({ taskId: "9001" });
        });
        const mergeButton = screen.getByTestId(
            "knowledge-graph-extraction-candidate-merge-apply-button"
        );
        await waitFor(() => {
            expect(mergeButton).toBeEnabled();
        });
        expect(
            screen.getByTestId("knowledge-graph-extraction-candidate-overwrite-apply-button")
        ).toBeEnabled();
        expect(
            screen.getByTestId("knowledge-graph-extraction-candidate-append-apply-button")
        ).toBeEnabled();
        fireEvent.click(
            screen.getByTestId("knowledge-graph-extraction-candidate-overwrite-apply-button")
        );
        await waitFor(() => {
            expect(workbenchServiceMocks.applyCandidate).toHaveBeenCalledWith({
                applyMode: "OVERWRITE",
                taskId: "9001"
            });
        });
        expect(screen.queryByRole("link", { name: "查看结果" })).not.toBeInTheDocument();
        expect(screen.queryByRole("link", { name: "进入精修" })).not.toBeInTheDocument();
    });

    it("disables workbench write actions without edit and apply permissions", async () => {
        replacePermissions(["knowledge:graph:view"]);

        renderPage();

        await selectVolumeAndManuscript();

        await waitFor(() => {
            expect(workbenchServiceMocks.getManuscript).toHaveBeenCalledWith({
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: "1001"
            });
        });
        const extractButton = screen.getByTestId(
            "knowledge-graph-extraction-manuscript-extract-button"
        );
        expect(extractButton).toBeDisabled();
        expect(screen.queryByRole("link", { name: "进入精修" })).not.toBeInTheDocument();

        fireEvent.click(extractButton);

        expect(workbenchServiceMocks.extractManuscript).not.toHaveBeenCalled();
        expect(workbenchServiceMocks.applyCandidate).not.toHaveBeenCalled();
    });

    it("does not load workbench data without graph view permission", async () => {
        replacePermissions([]);

        renderPage();

        await waitFor(() => {
            expect(workbenchServiceMocks.listManuscriptTree).not.toHaveBeenCalled();
            expect(serviceMocks.pageTasks).not.toHaveBeenCalled();
        });
    });

    it("loads workbench data after graph view permission arrives", async () => {
        clearPermissions();

        renderPage();

        await waitFor(() => {
            expect(workbenchServiceMocks.listManuscriptTree).not.toHaveBeenCalled();
        });

        replacePermissions(["knowledge:graph:view"]);

        await waitFor(() => {
            expect(workbenchServiceMocks.listManuscriptTree).toHaveBeenCalledWith({});
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
                sourceTaskId: "88",
                triggerSource: "REFINEMENT_APPLIED",
                replaceUnconfirmedOnly: true,
                selectionScopeJson: '{"sourceContentIds":[1001]}'
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
