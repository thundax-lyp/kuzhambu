import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { clearPermissions, replacePermissions } from "@/auth/permission-storage";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import { WangqiPage } from "./wangqi-page";
import { WangqiVersionPanel } from "./components/wangqi-version-panel";
import type { WangqiContentVersionRecord } from "./wangqi-types";

vi.mock("@/pages/classics/common/components/ai-candidate-panel", () => {
    const aiCandidatePanelMock = ({
        onApplied,
        onRejected
    }: {
        onApplied?: () => void;
        onRejected?: () => void;
    }) => {
        return (
            <div>
                <button onClick={onApplied}>mock-ai-applied</button>
                <button onClick={onRejected}>mock-ai-rejected</button>
            </div>
        );
    };

    return {
        AiCandidatePanel: aiCandidatePanelMock
    };
});

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({
        danger: vi.fn(({ onConfirm }) => onConfirm?.())
    })
}));

vi.mock("@/pages/classics/common/ai-refinement-task-service", () => ({
    createTask: vi.fn(() =>
        Promise.resolve({
            taskId: 9001,
            status: "PENDING",
            capability: "summary",
            contentType: "WANGQI_DOCUMENT",
            contentId: 1
        })
    ),
    getTaskFailureText: vi.fn(() => null),
    getTask: vi.fn(() =>
        Promise.resolve({
            taskId: 9001,
            status: "PENDING",
            capability: "summary",
            contentType: "WANGQI_DOCUMENT",
            contentId: 1
        })
    ),
    pageTasks: vi.fn(() => Promise.resolve({ items: [], total: 0, pageNo: 1, pageSize: 10 })),
    cancelTask: vi.fn()
}));

const apiResponse = (data: unknown) =>
    Promise.resolve(
        new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data }), {
            headers: { "Content-Type": "application/json" },
            status: 200
        })
    );

interface CapturedCall {
    body: unknown;
    method: string | undefined;
    path: string;
}

const capturedCalls: CapturedCall[] = [];
let mockDocumentRecord = {
    id: 1,
    title: "王圻文档",
    summary: "记录王圻古籍条目。",
    contentFormat: "MARKDOWN",
    content: "## 王圻",
    documentTime: "2026-01-01T00:00:00.000+00:00",
    storageObjectId: 7001,
    visibility: "PUBLIC"
};
let mockSummaryCandidates = [
    {
        candidateId: 5001,
        contentType: "WANGQI_DOCUMENT",
        contentId: 1,
        capability: "summary",
        objectId: null,
        resultFormat: "TEXT",
        resultPayload: "文档摘要候选",
        status: "PENDING",
        requestedAt: "2026-01-01T00:00:00.000+00:00"
    }
];
let mockTagCandidates = [
    {
        candidateId: 6001,
        contentType: "WANGQI_DOCUMENT",
        contentId: 1,
        capability: "tags",
        objectId: null,
        resultFormat: "STRUCTURED",
        resultPayload: JSON.stringify({ tags: ["经部", "文献"] }),
        status: "PENDING",
        requestedAt: "2026-01-01T00:00:00.000+00:00"
    }
];
let mockTagRecords = [
    {
        id: 8101,
        tagId: 9101,
        contentType: "WANGQI_DOCUMENT",
        contentId: 1,
        tagNameSnapshot: "史部",
        source: "MANUAL",
        status: "ACTIVE"
    }
];
let mockQaCandidates = [
    {
        candidateId: 7001,
        contentType: "WANGQI_DOCUMENT",
        contentId: 1,
        capability: "qa",
        objectId: null,
        resultFormat: "STRUCTURED",
        resultPayload: JSON.stringify({
            qaPairs: [{ question: "王圻是谁？", answer: "王圻是明代学者。" }]
        }),
        status: "PENDING",
        requestedAt: "2026-01-01T00:00:00.000+00:00"
    }
];
let mockQaRecords = [
    {
        id: 8201,
        contentType: "WANGQI_DOCUMENT",
        contentId: 1,
        question: "已有问题？",
        answer: "已有答案。",
        source: "MANUAL"
    }
];

const selectFirstRow = (table: HTMLElement) => {
    const checkbox = within(table).getAllByRole("checkbox")[1];
    fireEvent.click(checkbox.closest("label") ?? checkbox);
};

const waitForSelectableRow = async (table: HTMLElement) => {
    await waitFor(() => {
        expect(within(table).getAllByRole("checkbox").length).toBeGreaterThan(1);
    });
};

const openDrawerSection = async (user: ReturnType<typeof userEvent.setup>, sectionName: string) => {
    await user.click(await screen.findByText(sectionName));
};

const openTagsSection = async (user: ReturnType<typeof userEvent.setup>) => {
    await openDrawerSection(user, "标签");
};

const openQaSection = async (user: ReturnType<typeof userEvent.setup>) => {
    await openDrawerSection(user, "问答");
};

const readFetchUrl = (input: RequestInfo | URL) => {
    if (typeof input === "string") {
        return input;
    }
    if (input instanceof URL) {
        return input.href;
    }
    return input.url;
};

const readFetchBody = (body: BodyInit | null | undefined) => {
    if (!body) {
        return undefined;
    }
    return JSON.parse(String(body));
};

const createTestQueryClient = () =>
    new QueryClient({
        defaultOptions: {
            queries: {
                retry: false
            }
        }
    });

const installFetchMock = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
        const path = readFetchUrl(input).replace("/kuzhambu-admin-api/api", "");
        const body = readFetchBody(init?.body);
        capturedCalls.push({
            body,
            method: init?.method,
            path
        });
        if (path.endsWith("/classics/wangqi/documents/page")) {
            return apiResponse({
                pageNo: 1,
                pageSize: 20,
                totalCount: 1,
                count: 1,
                totalPage: 1,
                records: [mockDocumentRecord]
            });
        }
        if (path.endsWith("/classics/wangqi/documents/get")) {
            return apiResponse(mockDocumentRecord);
        }
        if (path.endsWith("/classics/wangqi/documents/timeline/list")) {
            return apiResponse([]);
        }
        if (
            path.endsWith(
                "/classics/wangqi/documents/" + String(mockDocumentRecord.id) + "/versions/list"
            )
        ) {
            return apiResponse([]);
        }
        if (
            path.endsWith("/classics/content/tags/list") &&
            body?.contentType === "WANGQI_DOCUMENT"
        ) {
            return apiResponse(mockTagRecords);
        }
        if (
            path.endsWith("/classics/content/qa-pairs/list") &&
            body?.contentType === "WANGQI_DOCUMENT"
        ) {
            return apiResponse(mockQaRecords);
        }
        if (path.endsWith("/classics/shares/batch/create")) {
            return apiResponse({
                failureCount: 1,
                failures: [
                    {
                        contentId: 2,
                        contentType: "WANGQI_DOCUMENT",
                        failureCode: "CONTENT_NOT_FOUND",
                        failureReason: "文档不存在",
                        status: "FAILED"
                    }
                ],
                successCount: 1,
                successes: [
                    {
                        contentId: 1,
                        contentType: "WANGQI_DOCUMENT",
                        resultId: 9101,
                        status: "ACTIVE"
                    }
                ]
            });
        }
        if (path.endsWith("/classics/content/visibility/change")) {
            return apiResponse({
                failureCount: 1,
                failures: [
                    {
                        contentId: 2,
                        contentType: "WANGQI_DOCUMENT",
                        failureCode: "BATCH_VISIBILITY_FAILED",
                        failureReason: "文档不存在",
                        status: "FAILED"
                    }
                ],
                successCount: 1,
                successes: [
                    {
                        contentId: 1,
                        contentType: "WANGQI_DOCUMENT",
                        resultId: 1,
                        status: "PRIVATE"
                    }
                ]
            });
        }
        if (path.endsWith("/ai/invocation/candidate/list")) {
            if (body?.capability === "tags") {
                return apiResponse(mockTagCandidates);
            }
            if (body?.capability === "qa") {
                return apiResponse(mockQaCandidates);
            }
            return apiResponse(mockSummaryCandidates);
        }
        if (path.endsWith("/classics/content/ai-candidates/change")) {
            return apiResponse({
                contentId: 1,
                contentType: "WANGQI_DOCUMENT",
                versionId: 9101,
                versionNo: 2
            });
        }
        if (path.endsWith("/classics/content/ai-candidates/batch/reject")) {
            return apiResponse({
                failureCount: 0,
                failures: [],
                successCount: 1,
                successes: [
                    {
                        candidateId: 5001,
                        contentId: 1,
                        contentType: "WANGQI_DOCUMENT",
                        capability: "summary",
                        objectId: null,
                        resultId: 5001,
                        status: "REJECTED"
                    }
                ]
            });
        }
        return apiResponse(true);
    });
};

describe("WangqiPage", () => {
    let queryClient: QueryClient;

    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(aiRefinementTaskService.pageTasks).mockResolvedValue({
            items: [],
            total: 0,
            pageNo: 1,
            pageSize: 10
        });
        queryClient = createTestQueryClient();
        capturedCalls.length = 0;
        mockDocumentRecord = {
            id: 1,
            title: "王圻文档",
            summary: "记录王圻古籍条目。",
            contentFormat: "MARKDOWN",
            content: "## 王圻",
            documentTime: "2026-01-01T00:00:00.000+00:00",
            storageObjectId: 7001,
            visibility: "PUBLIC"
        };
        mockSummaryCandidates = [
            {
                candidateId: 5001,
                contentType: "WANGQI_DOCUMENT",
                contentId: 1,
                capability: "summary",
                objectId: null,
                resultFormat: "TEXT",
                resultPayload: "文档摘要候选",
                status: "PENDING",
                requestedAt: "2026-01-01T00:00:00.000+00:00"
            }
        ];
        mockTagCandidates = [
            {
                candidateId: 6001,
                contentType: "WANGQI_DOCUMENT",
                contentId: 1,
                capability: "tags",
                objectId: null,
                resultFormat: "STRUCTURED",
                resultPayload: JSON.stringify({ tags: ["经部", "文献"] }),
                status: "PENDING",
                requestedAt: "2026-01-01T00:00:00.000+00:00"
            }
        ];
        mockTagRecords = [
            {
                id: 8101,
                tagId: 9101,
                contentType: "WANGQI_DOCUMENT",
                contentId: 1,
                tagNameSnapshot: "史部",
                source: "MANUAL",
                status: "ACTIVE"
            }
        ];
        mockQaCandidates = [
            {
                candidateId: 7001,
                contentType: "WANGQI_DOCUMENT",
                contentId: 1,
                capability: "qa",
                objectId: null,
                resultFormat: "STRUCTURED",
                resultPayload: JSON.stringify({
                    qaPairs: [{ question: "王圻是谁？", answer: "王圻是明代学者。" }]
                }),
                status: "PENDING",
                requestedAt: "2026-01-01T00:00:00.000+00:00"
            }
        ];
        mockQaRecords = [
            {
                id: 8201,
                contentType: "WANGQI_DOCUMENT",
                contentId: 1,
                question: "已有问题？",
                answer: "已有答案。",
                source: "MANUAL"
            }
        ];
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        replacePermissions([
            "classics:wangqi:view",
            "classics:wangqi:edit",
            "classics:sharing:edit",
            "classics:content:export",
            "discovery:qa:view"
        ]);
        installFetchMock();
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        clearPermissions();
        localStorage.clear();
        vi.restoreAllMocks();
    });

    it("renders page and first document", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "王圻文档" })).toBeInTheDocument();
        expect(await screen.findByText("王圻文档")).toBeInTheDocument();
    }, 30000);

    it("does not render a left document index", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        const table = await screen.findByLabelText("王圻文档表格");
        expect(table).toBeInTheDocument();
        expect(within(table).getByRole("columnheader", { name: "文档" })).toBeInTheDocument();
        expect(within(table).queryByRole("columnheader", { name: "摘要" })).not.toBeInTheDocument();
        expect(within(table).getByRole("columnheader", { name: "事件时间" })).toBeInTheDocument();
        expect(
            within(table).queryByRole("columnheader", { name: "原始文件对象 ID" })
        ).not.toBeInTheDocument();
        await within(table).findByTestId("wangqi-document-edit-1-button");
        expect(within(table).getByText("2026/01")).toBeInTheDocument();
        expect(screen.queryByLabelText("王圻文档索引")).not.toBeInTheDocument();
    }, 30000);

    it("renders multiline summary without table ellipsis", async () => {
        mockDocumentRecord = {
            ...mockDocumentRecord,
            summary: "第一行摘要\n第二行摘要"
        };

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        const firstLine = await screen.findByText("第一行摘要");
        const secondLine = await screen.findByText("第二行摘要");
        expect(firstLine).toHaveClass("wangqi-document-summary-line");
        expect(secondLine).toHaveClass("wangqi-document-summary-line");
        expect(firstLine.closest(".wangqi-document-summary-preview")).toBeInTheDocument();
    }, 30000);

    it("does not render an empty summary placeholder in the document cell", async () => {
        mockDocumentRecord = {
            ...mockDocumentRecord,
            summary: ""
        };

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        const table = await screen.findByLabelText("王圻文档表格");
        await within(table).findByTestId("wangqi-document-edit-1-button");

        expect(within(table).queryByText("暂无摘要")).not.toBeInTheDocument();
    }, 30000);

    it("keeps summary and markdown editor in the basic drawer section", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("wangqi-document-edit-1-button"));

        expect(await screen.findByRole("radio", { name: "基础信息" })).toBeChecked();
        expect(screen.queryByRole("radio", { name: "摘要" })).not.toBeInTheDocument();
        expect(screen.queryByRole("radio", { name: "正文" })).not.toBeInTheDocument();
        expect(screen.queryByRole("radio", { name: "内容处理" })).not.toBeInTheDocument();
        expect(screen.getByRole("radio", { name: "标签" })).toBeInTheDocument();
        expect(screen.getByRole("radio", { name: "问答" })).toBeInTheDocument();

        expect(await screen.findByLabelText("王圻文档摘要")).toHaveValue("记录王圻古籍条目。");
        expect(screen.getByRole("button", { name: "AI 摘要" })).toBeInTheDocument();
        expect(await screen.findByLabelText("王圻 Tiptap 编辑器")).toBeInTheDocument();
        expect(screen.getByLabelText("王圻文档正文")).toHaveAttribute("contenteditable", "true");
        expect(screen.getByTestId("classics-wangqi-markdown-heading-button")).toBeInTheDocument();
        expect(screen.queryByLabelText("王圻文档正文预览")).not.toBeInTheDocument();
    }, 30000);

    it("opens summary ai modal and locks create while task is running", async () => {
        const user = userEvent.setup();

        vi.mocked(aiRefinementTaskService.pageTasks).mockImplementation(async (query) => {
            if (query?.contentType === "WANGQI_DOCUMENT" && query.contentId === 1) {
                return {
                    items: [
                        {
                            taskId: 9001,
                            status: "RUNNING",
                            capability: "summary",
                            contentType: "WANGQI_DOCUMENT",
                            contentId: 1,
                            requestedAt: "2026-01-01T00:00:00.000+00:00"
                        }
                    ],
                    total: 1,
                    pageNo: 1,
                    pageSize: 10
                };
            }

            return { items: [], total: 0, pageNo: 1, pageSize: 10 };
        });

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("wangqi-document-edit-1-button"));
        await user.click(await screen.findByRole("button", { name: "AI 摘要" }));

        expect(await screen.findByText("摘要任务排队中")).toBeInTheDocument();
        const generateButton = await screen.findByTestId(
            "classics-wangqi-document-summary-ai-generate-button"
        );
        expect(generateButton).toBeDisabled();
        expect(screen.getByLabelText("AI摘要当前摘要")).toHaveValue("记录王圻古籍条目。");
        expect(screen.getByLabelText("AI摘要参考正文")).toHaveValue("## 王圻");
        expect(await screen.findByLabelText("AI摘要候选摘要")).toBeDisabled();
        expect(aiRefinementTaskService.createTask).not.toHaveBeenCalled();

        await user.click(generateButton);

        expect(aiRefinementTaskService.createTask).not.toHaveBeenCalled();
    }, 30000);

    it("applies summary ai draft back to the basic form", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("wangqi-document-edit-1-button"));
        await user.click(await screen.findByRole("button", { name: "AI 摘要" }));
        const candidateSummaryInput = await screen.findByLabelText("AI摘要候选摘要");
        await waitFor(() => {
            expect(candidateSummaryInput).toBeEnabled();
        });
        await user.clear(candidateSummaryInput);
        await user.type(candidateSummaryInput, "采用后的摘要");
        await user.click(screen.getByTestId("classics-wangqi-document-summary-ai-apply-button"));

        await waitFor(() => {
            expect(screen.getByLabelText("王圻文档摘要")).toHaveValue("采用后的摘要");
        });
    }, 30000);

    it("tracks generated summary task until the returned candidate fills the draft", async () => {
        const user = userEvent.setup();

        vi.mocked(aiRefinementTaskService.createTask).mockImplementationOnce(async () => {
            mockSummaryCandidates = [
                ...mockSummaryCandidates,
                {
                    candidateId: 5002,
                    contentType: "WANGQI_DOCUMENT",
                    contentId: 1,
                    capability: "summary",
                    objectId: null,
                    resultFormat: "TEXT",
                    resultPayload: "新生成摘要候选",
                    status: "PENDING",
                    requestedAt: "2026-01-01T00:01:00.000+00:00"
                }
            ];
            return {
                taskId: 9100,
                status: "PENDING",
                capability: "summary",
                contentType: "WANGQI_DOCUMENT",
                contentId: 1
            };
        });
        vi.mocked(aiRefinementTaskService.getTask).mockResolvedValueOnce({
            taskId: 9100,
            status: "SUCCEEDED",
            capability: "summary",
            contentType: "WANGQI_DOCUMENT",
            contentId: 1,
            candidateId: 5002,
            requestedAt: "2026-01-01T00:01:00.000+00:00"
        });

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("wangqi-document-edit-1-button"));
        await user.click(await screen.findByRole("button", { name: "AI 摘要" }));
        await waitFor(() => {
            expect(screen.getByLabelText("AI摘要候选摘要")).toHaveValue("文档摘要候选");
        });

        await user.click(screen.getByTestId("classics-wangqi-document-summary-ai-generate-button"));

        await waitFor(() =>
            expect(aiRefinementTaskService.getTask).toHaveBeenCalledWith({ taskId: 9100 })
        );
        await waitFor(() => {
            expect(screen.getByLabelText("AI摘要候选摘要")).toHaveValue("新生成摘要候选");
        });
    }, 30000);

    it("creates summary tags and qa refinement tasks from their own sections", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("wangqi-document-edit-1-button"));
        await user.click(await screen.findByRole("button", { name: "AI 摘要" }));
        await user.click(
            await screen.findByTestId("classics-wangqi-document-summary-ai-generate-button")
        );
        await waitFor(() => expect(aiRefinementTaskService.createTask).toHaveBeenCalledTimes(1));
        await openTagsSection(user);
        await user.click(await screen.findByTestId("classics-wangqi-document-tags-ai-button"));
        await user.click(
            await screen.findByTestId("classics-wangqi-document-tags-ai-generate-button")
        );
        await waitFor(() => expect(aiRefinementTaskService.createTask).toHaveBeenCalledTimes(2));
        await openQaSection(user);
        await user.click(await screen.findByTestId("classics-wangqi-document-qa-ai-button"));
        await user.click(
            await screen.findByTestId("classics-wangqi-document-qa-ai-generate-button")
        );
        await waitFor(() => expect(aiRefinementTaskService.createTask).toHaveBeenCalledTimes(3));

        const calls = vi
            .mocked(aiRefinementTaskService.createTask)
            .mock.calls.map(([payload]) => payload);
        expect(calls.map((payload) => payload.capability)).toEqual(["summary", "tags", "qa"]);
        calls.forEach((payload) => {
            expect(payload).toEqual(
                expect.objectContaining({
                    scope: "classics",
                    contentType: "WANGQI_DOCUMENT",
                    contentId: 1,
                    serviceRole: "PRIMARY",
                    modelId: 1,
                    modelName: "gpt-5.5",
                    locale: "zh-CN"
                })
            );
            expect(payload).not.toHaveProperty("requestedBy");
            expect(payload.requestId).toContain(`wangqi-${payload.capability}-request`);
            expect(payload.traceId).toContain(`wangqi-${payload.capability}-trace`);
        });
    }, 30000);

    it("opens qa ai modal tracks task and applies candidate qa pairs", async () => {
        const user = userEvent.setup();

        vi.mocked(aiRefinementTaskService.createTask).mockResolvedValueOnce({
            taskId: 9300,
            status: "PENDING",
            capability: "qa",
            contentType: "WANGQI_DOCUMENT",
            contentId: 1
        });
        vi.mocked(aiRefinementTaskService.getTask).mockResolvedValueOnce({
            taskId: 9300,
            status: "SUCCEEDED",
            capability: "qa",
            contentType: "WANGQI_DOCUMENT",
            contentId: 1,
            candidateId: 7001,
            requestedAt: "2026-01-01T00:01:00.000+00:00"
        });

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("wangqi-document-edit-1-button"));
        await openQaSection(user);
        await user.click(await screen.findByTestId("classics-wangqi-document-qa-ai-button"));
        expect(
            await screen.findByTestId("classics-wangqi-document-qa-ai-modal")
        ).toBeInTheDocument();
        expect(await screen.findByLabelText("问答依据标题")).toHaveValue("王圻文档");
        expect(screen.getByLabelText("问答依据摘要")).toHaveValue("记录王圻古籍条目。");
        expect(screen.getByLabelText("问答依据正文")).toHaveValue("## 王圻");
        expect(screen.getByLabelText("问答依据已有问答")).toHaveTextContent("已有问题？");

        await user.click(screen.getByTestId("classics-wangqi-document-qa-ai-generate-button"));

        await waitFor(() => expect(aiRefinementTaskService.createTask).toHaveBeenCalledTimes(1));
        const taskPayload = vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0];
        expect(taskPayload?.promptMessagesJson).toContain("已有问答：Q：已有问题？ A：已有答案。");
        expect(taskPayload?.promptVariablesJson).toContain(
            '"existingQaPairs":[{"question":"已有问题？","answer":"已有答案。"}]'
        );
        expect(taskPayload?.inputPayloadJson).toContain(
            '"existingQaPairs":[{"question":"已有问题？","answer":"已有答案。"}]'
        );
        await waitFor(() =>
            expect(aiRefinementTaskService.getTask).toHaveBeenCalledWith({ taskId: 9300 })
        );
        expect(await screen.findByText("问答任务已完成")).toBeInTheDocument();
        expect(await screen.findByLabelText("问答问题 1")).toHaveValue("王圻是谁？");
        expect(screen.getByLabelText("问答答案 1")).toHaveValue("王圻是明代学者。");

        await user.click(screen.getByTestId("classics-wangqi-document-qa-ai-apply-button"));

        await waitFor(() => {
            expect(
                capturedCalls.find((call) =>
                    call.path.endsWith("/classics/content/ai-candidates/change")
                )?.body
            ).toEqual(
                expect.objectContaining({
                    candidateId: 7001,
                    contentId: 1,
                    contentType: "WANGQI_DOCUMENT",
                    capability: "qa",
                    resultFormat: "STRUCTURED",
                    resultPayload: JSON.stringify({
                        qaPairs: [{ question: "王圻是谁？", answer: "王圻是明代学者。" }]
                    })
                })
            );
        });
    }, 30000);

    it("opens tag ai modal tracks task and applies candidate tags", async () => {
        const user = userEvent.setup();

        vi.mocked(aiRefinementTaskService.createTask).mockResolvedValueOnce({
            taskId: 9200,
            status: "PENDING",
            capability: "tags",
            contentType: "WANGQI_DOCUMENT",
            contentId: 1
        });
        vi.mocked(aiRefinementTaskService.getTask).mockResolvedValueOnce({
            taskId: 9200,
            status: "SUCCEEDED",
            capability: "tags",
            contentType: "WANGQI_DOCUMENT",
            contentId: 1,
            candidateId: 6001,
            requestedAt: "2026-01-01T00:01:00.000+00:00"
        });

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("wangqi-document-edit-1-button"));
        await openTagsSection(user);
        await user.click(await screen.findByTestId("classics-wangqi-document-tags-ai-button"));
        expect(
            await screen.findByTestId("classics-wangqi-document-tags-ai-modal")
        ).toBeInTheDocument();
        expect(await screen.findByLabelText("AI标签依据标题")).toHaveValue("王圻文档");
        expect(screen.getByLabelText("AI标签依据摘要")).toHaveValue("记录王圻古籍条目。");
        expect(screen.getByLabelText("AI标签依据正文")).toHaveValue("## 王圻");
        expect(screen.getByLabelText("AI标签依据已有标签")).toHaveTextContent("史部");

        await user.click(screen.getByTestId("classics-wangqi-document-tags-ai-generate-button"));

        await waitFor(() => expect(aiRefinementTaskService.createTask).toHaveBeenCalledTimes(1));
        const taskPayload = vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0];
        expect(taskPayload?.promptMessagesJson).toContain("已有标签：史部");
        expect(taskPayload?.promptVariablesJson).toContain('"existingTags":["史部"]');
        expect(taskPayload?.inputPayloadJson).toContain('"existingTags":["史部"]');
        await waitFor(() =>
            expect(aiRefinementTaskService.getTask).toHaveBeenCalledWith({ taskId: 9200 })
        );
        expect(await screen.findByText("标签任务已完成")).toBeInTheDocument();
        expect(await screen.findByLabelText("候选标签 1")).toHaveValue("经部");
        expect(screen.getByLabelText("候选标签 2")).toHaveValue("文献");

        await user.click(screen.getByTestId("classics-wangqi-document-tags-ai-apply-button"));

        await waitFor(() => {
            expect(
                capturedCalls.find((call) =>
                    call.path.endsWith("/classics/content/ai-candidates/change")
                )?.body
            ).toEqual(
                expect.objectContaining({
                    candidateId: 6001,
                    contentId: 1,
                    contentType: "WANGQI_DOCUMENT",
                    capability: "tags",
                    resultFormat: "STRUCTURED",
                    resultPayload: JSON.stringify({ tags: ["经部", "文献"] })
                })
            );
        });
    }, 30000);

    it("does not create refinement task when document content is empty", async () => {
        const user = userEvent.setup();
        mockDocumentRecord = {
            ...mockDocumentRecord,
            content: ""
        };

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("wangqi-document-edit-1-button"));
        await openTagsSection(user);
        await user.click(await screen.findByTestId("classics-wangqi-document-tags-ai-button"));
        await user.click(
            await screen.findByTestId("classics-wangqi-document-tags-ai-generate-button")
        );

        expect(aiRefinementTaskService.createTask).not.toHaveBeenCalled();
        expect(await screen.findByText("正文为空，无法创建 AI 精修任务")).toBeInTheDocument();
    }, 30000);

    it("opens single document QA from the document drawer", async () => {
        const user = userEvent.setup();
        const openSpy = vi.spyOn(window, "open").mockImplementation(() => null);

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("wangqi-document-edit-1-button"));
        await openQaSection(user);
        const qaButton = await screen.findByRole("button", { name: "单文档问答" });

        expect(qaButton).toBeEnabled();
        await user.click(qaButton);

        expect(openSpy).toHaveBeenCalledWith(
            "/discovery/qa?contextContentId=1&contextContentType=WANGQI_DOCUMENT&contextMode=SINGLE_DOCUMENT&title=%E7%8E%8B%E5%9C%BB%E6%96%87%E6%A1%A3",
            "_blank",
            "noopener,noreferrer"
        );
    }, 30000);

    it("disables single document QA when document id is missing", async () => {
        const user = userEvent.setup();
        mockDocumentRecord = {
            ...mockDocumentRecord,
            id: 0
        };

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("wangqi-document-edit-0-button"));
        await openQaSection(user);

        expect(await screen.findByRole("button", { name: "单文档问答" })).toBeDisabled();
    }, 30000);

    it("disables single document QA without discovery QA permission", async () => {
        const user = userEvent.setup();
        replacePermissions([
            "classics:wangqi:view",
            "classics:wangqi:edit",
            "classics:sharing:edit",
            "classics:content:export"
        ]);

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("wangqi-document-edit-1-button"));
        await openQaSection(user);

        expect(await screen.findByRole("button", { name: "单文档问答" })).toBeDisabled();
    }, 30000);

    it("creates batch shares from selected documents and shows item failures", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        const table = await screen.findByLabelText("王圻文档表格");
        await waitForSelectableRow(table);
        const batchShareButton = screen.getByTestId("classics-wangqi-wangqi-batch-share-button");
        selectFirstRow(table);
        await waitFor(() => {
            expect(batchShareButton).not.toBeDisabled();
        });
        await user.click(batchShareButton);

        await waitFor(() => {
            expect(screen.getByText("分享结果：成功 1，失败 1")).toBeInTheDocument();
        });
        expect(screen.getByText("WANGQI_DOCUMENT#2: 文档不存在")).toBeInTheDocument();
    }, 30000);

    it("changes selected documents visibility and shows item failures", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        const table = await screen.findByLabelText("王圻文档表格");
        await waitForSelectableRow(table);
        const batchPrivateButton = screen.getByTestId(
            "classics-wangqi-wangqi-batch-private-button"
        );
        selectFirstRow(table);
        await waitFor(() => {
            expect(batchPrivateButton).not.toBeDisabled();
        });
        await user.click(batchPrivateButton);

        await waitFor(() => {
            expect(screen.getByText("可见性结果：成功 1，失败 1")).toBeInTheDocument();
        });
        expect(capturedCalls).toContainEqual({
            body: {
                contentIds: [1],
                contentType: "WANGQI_DOCUMENT",
                visibility: "PRIVATE"
            },
            method: "POST",
            path: "/classics/content/visibility/change"
        });
        expect(screen.getByText("WANGQI_DOCUMENT#2: 文档不存在")).toBeInTheDocument();
    }, 30000);

    it("opens batch candidate governance drawer from selected documents", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        const table = await screen.findByLabelText("王圻文档表格");
        await waitForSelectableRow(table);
        const batchCandidateButton = screen.getByRole("button", { name: "候选治理" });

        expect(batchCandidateButton).toBeDisabled();
        selectFirstRow(table);
        await waitFor(() => {
            expect(batchCandidateButton).not.toBeDisabled();
        });
        await user.click(batchCandidateButton);

        expect(await screen.findByText("AI 候选批量治理")).toBeInTheDocument();
        expect(screen.getByText(/已选内容\s*1\s*个/)).toBeInTheDocument();
        const candidateTable = await screen.findByLabelText("AI 候选批量治理列表");
        const candidateCheckbox = within(candidateTable).getAllByRole("checkbox")[1];
        expect(candidateCheckbox).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "批量应用" })).toBeEnabled();
        expect(screen.getByRole("button", { name: "批量拒绝" })).toBeEnabled();
        expect(capturedCalls).toContainEqual({
            body: {
                contentType: "WANGQI_DOCUMENT",
                contentId: 1,
                status: "PENDING"
            },
            method: "POST",
            path: "/ai/invocation/candidate/list"
        });

        await user.click(candidateCheckbox);
        await user.click(screen.getByRole("button", { name: "批量拒绝" }));

        expect(
            (await screen.findAllByText("批量候选拒绝结果：成功 1，失败 0")).length
        ).toBeGreaterThanOrEqual(1);
        expect(capturedCalls).toContainEqual({
            body: {
                errorType: "USER_REJECTED",
                errorMessage: "用户已批量拒绝该 AI 候选",
                items: [
                    {
                        candidateId: 5001,
                        contentType: "WANGQI_DOCUMENT",
                        contentId: 1,
                        capability: "summary",
                        objectId: null
                    }
                ]
            },
            method: "POST",
            path: "/classics/content/ai-candidates/batch/reject"
        });
    }, 30000);

    it("refreshes detail and tags/qa/version after ai candidate apply", async () => {
        const user = userEvent.setup();
        const invalidateSpy = vi.spyOn(queryClient, "invalidateQueries");

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("wangqi-document-edit-1-button"));
        await openTagsSection(user);
        capturedCalls.length = 0;
        await user.click(await screen.findByRole("button", { name: "mock-ai-applied" }));

        await waitFor(() => {
            expect(
                invalidateSpy.mock.calls.some(
                    ([arg]) =>
                        JSON.stringify(arg?.queryKey) === JSON.stringify(["wangqi", "detail"])
                )
            ).toBeTruthy();
            expect(
                invalidateSpy.mock.calls.some(
                    ([arg]) =>
                        JSON.stringify(arg?.queryKey) ===
                        JSON.stringify(["classics", "content", "tags", "WANGQI_DOCUMENT"])
                )
            ).toBeTruthy();
            expect(
                invalidateSpy.mock.calls.some(
                    ([arg]) =>
                        JSON.stringify(arg?.queryKey) ===
                        JSON.stringify(["classics", "content", "qa-pairs", "WANGQI_DOCUMENT"])
                )
            ).toBeTruthy();
            expect(
                invalidateSpy.mock.calls.some(
                    ([arg]) =>
                        JSON.stringify(arg?.queryKey) === JSON.stringify(["wangqi", "versions"])
                )
            ).toBeTruthy();
        });
    }, 30000);

    it("only refreshes candidate list after ai candidate reject", async () => {
        const user = userEvent.setup();
        const invalidateSpy = vi.spyOn(queryClient, "invalidateQueries");

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("wangqi-document-edit-1-button"));
        await openTagsSection(user);
        capturedCalls.length = 0;
        await user.click(await screen.findByRole("button", { name: "mock-ai-rejected" }));

        await waitFor(() => {
            expect(
                invalidateSpy.mock.calls.some(
                    ([arg]) =>
                        JSON.stringify(arg?.queryKey) ===
                        JSON.stringify([
                            "ai",
                            "candidates",
                            "WANGQI_DOCUMENT",
                            mockDocumentRecord.id
                        ])
                )
            ).toBeTruthy();
        });

        expect(
            invalidateSpy.mock.calls.every(
                ([arg]) =>
                    JSON.stringify(arg?.queryKey) ===
                    JSON.stringify(["ai", "candidates", "WANGQI_DOCUMENT", mockDocumentRecord.id])
            )
        ).toBeTruthy();
    }, 30000);

    it("renders wangqi version history panel with confirmed tags and qa snapshots", () => {
        const version: WangqiContentVersionRecord = {
            id: 400000000003,
            versionNo: 6,
            changeType: "AI_APPLIED",
            changeSummary: "更新快照",
            versionedAt: "2026-06-01T00:00:00.000+00:00",
            snapshotJson: JSON.stringify({
                title: "快照标题",
                summary: "快照摘要",
                contentFormat: "MARKDOWN",
                content: "快照正文",
                documentTime: "明年正月初一",
                storageObjectId: 7001,
                visibility: "PUBLIC",
                tags: [
                    {
                        id: 5001,
                        tagId: 6001,
                        tagNameSnapshot: "文献"
                    },
                    {
                        id: 5002,
                        tagId: 6002,
                        tagNameSnapshot: "校勘"
                    }
                ],
                qaPairs: [
                    {
                        id: 7001,
                        question: "什么是经文注释？",
                        answer: "为文献加注释与解释。"
                    },
                    {
                        id: 7002,
                        question: "应用来源有哪些？",
                        answer: "来自专家校对。"
                    }
                ]
            })
        };

        render(
            <WangqiVersionPanel
                currentDocument={{
                    id: 1,
                    title: "正文标题",
                    summary: "正文摘要",
                    contentFormat: "MARKDOWN",
                    content: "正文正文",
                    documentTime: "2026-01-01",
                    storageObjectId: 7001,
                    visibility: "PUBLIC"
                }}
                versions={[version]}
                selectedVersion={version}
                detailLoading={false}
                listLoading={false}
                onSelectVersion={vi.fn()}
                onResetVersion={vi.fn()}
            />
        );

        expect(screen.getByText("确认标签")).toBeInTheDocument();
        expect(screen.getByText("文献")).toBeInTheDocument();
        expect(screen.getByText("校勘")).toBeInTheDocument();
        expect(
            screen.getByText("Q: 什么是经文注释？；A: 为文献加注释与解释。")
        ).toBeInTheDocument();
        expect(screen.getByText("Q: 应用来源有哪些？；A: 来自专家校对。")).toBeInTheDocument();
    });
});
