import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { clearPermissions, replacePermissions } from "@/auth/permission-storage";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as currentUserService from "@/service/current-user-service";
import { WangqiPage } from "./wangqi-page";
import { WangqiVersionHistoryPanel } from "./components/wangqi-version-history-panel";
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

vi.mock("@/service/current-user-service", () => ({
    getCurrentUserInfo: vi.fn(() => Promise.resolve({ id: 99, loginName: "admin", name: "Admin" }))
}));

vi.mock("@/pages/classics/common/ai-refinement-task-service", () => ({
    createTask: vi.fn(() =>
        Promise.resolve({ id: 9001, status: "PENDING", capability: "summary" })
    ),
    getTask: vi.fn(),
    pageTasks: vi.fn(() => Promise.resolve({ items: [], totalCount: 0, pageNo: 1, pageSize: 10 })),
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
    id: 400000000001,
    title: "王圻文档",
    summary: "记录王圻古籍条目。",
    contentFormat: "MARKDOWN",
    content: "## 王圻",
    documentTime: "2026-01-01T00:00:00.000+00:00",
    storageObjectId: 7001,
    visibility: "PUBLIC"
};

const selectFirstRow = (table: HTMLElement) => {
    const checkbox = within(table).getAllByRole("checkbox")[1];
    fireEvent.click(checkbox.closest("label") ?? checkbox);
};

const waitForSelectableRow = async (table: HTMLElement) => {
    await waitFor(() => {
        expect(within(table).getAllByRole("checkbox").length).toBeGreaterThan(1);
    });
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
        capturedCalls.push({
            body: readFetchBody(init?.body),
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
        if (path.endsWith(`/classics/wangqi/documents/${mockDocumentRecord.id}/get`)) {
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
            path.includes("/classics/content/tags") &&
            path.includes("contentType=WANGQI_DOCUMENT")
        ) {
            return apiResponse([]);
        }
        if (
            path.includes("/classics/content/qa-pairs") &&
            path.includes("contentType=WANGQI_DOCUMENT")
        ) {
            return apiResponse([]);
        }
        if (path.endsWith("/classics/shares/batch/create")) {
            return apiResponse({
                failureCount: 1,
                failures: [
                    {
                        contentId: 400000000002,
                        contentType: "WANGQI_DOCUMENT",
                        failureCode: "CONTENT_NOT_FOUND",
                        failureReason: "文档不存在",
                        status: "FAILED"
                    }
                ],
                successCount: 1,
                successes: [
                    {
                        contentId: 400000000001,
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
                        contentId: 400000000002,
                        contentType: "WANGQI_DOCUMENT",
                        failureCode: "BATCH_VISIBILITY_FAILED",
                        failureReason: "文档不存在",
                        status: "FAILED"
                    }
                ],
                successCount: 1,
                successes: [
                    {
                        contentId: 400000000001,
                        contentType: "WANGQI_DOCUMENT",
                        resultId: 400000000001,
                        status: "PRIVATE"
                    }
                ]
            });
        }
        if (path.endsWith("/ai/invocation/candidate/list")) {
            return apiResponse([
                {
                    candidateId: 5001,
                    contentType: "WANGQI_DOCUMENT",
                    contentId: 400000000001,
                    capability: "summary",
                    objectId: null,
                    resultFormat: "TEXT",
                    resultPayload: "文档摘要候选",
                    status: "PENDING",
                    requestedAt: "2026-01-01T00:00:00.000+00:00"
                }
            ]);
        }
        if (path.endsWith("/classics/content/ai-candidates/batch/reject")) {
            return apiResponse({
                failureCount: 0,
                failures: [],
                successCount: 1,
                successes: [
                    {
                        candidateId: 5001,
                        contentId: 400000000001,
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
        queryClient = createTestQueryClient();
        capturedCalls.length = 0;
        mockDocumentRecord = {
            id: 400000000001,
            title: "王圻文档",
            summary: "记录王圻古籍条目。",
            contentFormat: "MARKDOWN",
            content: "## 王圻",
            documentTime: "2026-01-01T00:00:00.000+00:00",
            storageObjectId: 7001,
            visibility: "PUBLIC"
        };
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

    it("creates summary tags and qa refinement tasks from the document drawer", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <WangqiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByRole("button", { name: "编辑王圻文档 王圻文档" }));
        await user.click(await screen.findByRole("button", { name: "创建摘要任务" }));
        await waitFor(() => expect(aiRefinementTaskService.createTask).toHaveBeenCalledTimes(1));
        await user.click(await screen.findByRole("button", { name: "创建标签任务" }));
        await waitFor(() => expect(aiRefinementTaskService.createTask).toHaveBeenCalledTimes(2));
        await user.click(await screen.findByRole("button", { name: "创建问答任务" }));
        await waitFor(() => expect(aiRefinementTaskService.createTask).toHaveBeenCalledTimes(3));

        expect(currentUserService.getCurrentUserInfo).toHaveBeenCalled();
        const calls = vi
            .mocked(aiRefinementTaskService.createTask)
            .mock.calls.map(([payload]) => payload);
        expect(calls.map((payload) => payload.capability)).toEqual(["summary", "tags", "qa"]);
        calls.forEach((payload) => {
            expect(payload).toEqual(
                expect.objectContaining({
                    scope: "classics",
                    contentType: "WANGQI_DOCUMENT",
                    contentId: 400000000001,
                    requestedBy: 99,
                    serviceRole: "PRIMARY",
                    modelId: 1,
                    modelName: "gpt-5.5",
                    locale: "zh-CN"
                })
            );
            expect(payload.requestId).toContain(`wangqi-${payload.capability}-request`);
            expect(payload.traceId).toContain(`wangqi-${payload.capability}-trace`);
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

        await user.click(await screen.findByRole("button", { name: "编辑王圻文档 王圻文档" }));
        await user.click(await screen.findByRole("button", { name: "创建标签任务" }));

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

        await user.click(await screen.findByRole("button", { name: "编辑王圻文档 王圻文档" }));
        const qaButton = await screen.findByRole("button", { name: "单文档问答" });

        expect(qaButton).toBeEnabled();
        await user.click(qaButton);

        expect(openSpy).toHaveBeenCalledWith(
            "/discovery/qa?contextContentId=400000000001&contextContentType=WANGQI_DOCUMENT&contextMode=SINGLE_DOCUMENT&title=%E7%8E%8B%E5%9C%BB%E6%96%87%E6%A1%A3",
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

        await user.click(await screen.findByRole("button", { name: "编辑王圻文档 王圻文档" }));

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

        await user.click(await screen.findByRole("button", { name: "编辑王圻文档 王圻文档" }));

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
        const batchShareButton = screen.getByRole("button", { name: "批量分享" });
        selectFirstRow(table);
        await waitFor(() => {
            expect(batchShareButton).not.toBeDisabled();
        });
        await user.click(batchShareButton);

        await waitFor(() => {
            expect(screen.getByText("批量分享结果：成功 1，失败 1")).toBeInTheDocument();
        });
        expect(screen.getByText("WANGQI_DOCUMENT#400000000002: 文档不存在")).toBeInTheDocument();
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
        const batchPrivateButton = screen.getByRole("button", { name: "批量私有" });
        selectFirstRow(table);
        await waitFor(() => {
            expect(batchPrivateButton).not.toBeDisabled();
        });
        await user.click(batchPrivateButton);

        await waitFor(() => {
            expect(screen.getByText("批量可见性结果：成功 1，失败 1")).toBeInTheDocument();
        });
        expect(capturedCalls).toContainEqual({
            body: {
                contentIds: [400000000001],
                contentType: "WANGQI_DOCUMENT",
                visibility: "PRIVATE"
            },
            method: "POST",
            path: "/classics/content/visibility/change"
        });
        expect(screen.getByText("WANGQI_DOCUMENT#400000000002: 文档不存在")).toBeInTheDocument();
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
        const batchCandidateButton = screen.getByRole("button", { name: "批量候选治理" });

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
                contentId: 400000000001,
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
                        contentId: 400000000001,
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

        await user.click(await screen.findByRole("button", { name: "编辑王圻文档 王圻文档" }));
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

        await user.click(await screen.findByRole("button", { name: "编辑王圻文档 王圻文档" }));
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
            <WangqiVersionHistoryPanel
                currentDocument={{
                    id: 400000000001,
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
