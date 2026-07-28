import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { clearPermissions, replacePermissions } from "@/auth/permission-storage";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import { MingCustomsVersionHistoryPanel } from "./components/ming-customs-version-history-panel";
import { MingCustomsPage } from "./ming-customs-page";
import type { MingCustomsContentVersionRecord } from "./ming-customs-types";

const confirmDangerMock = vi.hoisted(() =>
    vi.fn((options: { onConfirm?: () => void }) => options.onConfirm?.())
);

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({
        danger: confirmDangerMock
    })
}));

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

vi.mock("@/pages/classics/common/ai-refinement-task-service", () => ({
    createTask: vi.fn(() =>
        Promise.resolve({ id: "9101", status: "PENDING", capability: "summary" })
    ),
    getTask: vi.fn(),
    pageTasks: vi.fn(() => Promise.resolve({ items: [], totalCount: 0, pageNo: 1, pageSize: 10 })),
    cancelTask: vi.fn(),
    getNormalizedTaskCapability: vi.fn((capability: string) => {
        const aliases: Record<string, string> = {
            classics_translate: "translate",
            classics_summary: "summary",
            classics_tags: "tags",
            classics_qa: "qa",
            classics_image_describe: "image_analysis",
            classics_image_prompt_fusion: "fusion",
            classics_visual_describe: "visual",
            classics_image_generate: "image_gen"
        };
        return aliases[capability] ?? capability;
    })
}));

const apiResponse = (data: unknown) =>
    Promise.resolve(
        new Response(
            JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data
            }),
            {
                headers: { "Content-Type": "application/json" },
                status: 200
            }
        )
    );

interface CapturedCall {
    body: unknown;
    method: string | undefined;
    path: string;
}

const capturedCalls: CapturedCall[] = [];
let mockMingCustomsRecord = {
    id: "500000000001",
    title: "岁时礼仪：元旦朝贺",
    category: "RITUAL",
    chapter: "岁时礼仪",
    section: "正旦",
    summary: "记录明代正旦朝贺与家族拜礼。",
    contentFormat: "MARKDOWN",
    content: "## 正旦",
    originalExcerpts: "正旦朝贺。",
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

        if (path.endsWith("/classics/ming-customs/page")) {
            return apiResponse({
                pageNo: 1,
                pageSize: 20,
                totalCount: 1,
                count: 1,
                totalPage: 1,
                records: [mockMingCustomsRecord]
            });
        }

        if (path.endsWith("/classics/ming-customs/keyword-cloud/list")) {
            return apiResponse([]);
        }

        if (path.endsWith("/classics/ming-customs/tag-cloud/list")) {
            return apiResponse([
                {
                    tagId: "700000000001",
                    tagNameSnapshot: "礼制",
                    count: 3
                }
            ]);
        }

        if (path.endsWith("/sys/dict/page")) {
            return apiResponse({
                pageNo: 1,
                pageSize: 100,
                totalCount: 1,
                count: 1,
                totalPage: 1,
                records: [
                    {
                        type: "CLASSICS_MING_CUSTOMS_CATEGORY",
                        value: "RITUAL",
                        label: "礼制"
                    }
                ]
            });
        }

        if (path.endsWith("/classics/content/exports/page")) {
            return apiResponse({
                pageNo: 1,
                pageSize: 8,
                totalPage: 1,
                count: 0,
                records: [],
                totalCount: 0
            });
        }
        if (path.endsWith("/classics/ming-customs/get")) {
            return apiResponse(mockMingCustomsRecord);
        }
        if (path.endsWith("/classics/ming-customs/versions/list")) {
            return apiResponse([
                {
                    id: "9001",
                    contentType: "MING_CUSTOMS",
                    contentId: "500000000001",
                    versionNo: 1,
                    versionedAt: "2026-01-01T00:00:00.000+00:00",
                    snapshotJson: JSON.stringify({
                        title: "旧标题",
                        category: "RITUAL",
                        chapter: "岁时礼仪",
                        section: "正旦",
                        summary: "旧版摘要",
                        contentFormat: "MARKDOWN",
                        content: "## 旧版",
                        originalExcerpts: "旧版摘录",
                        visibility: "PUBLIC"
                    }),
                    changeType: "HISTORY_RESTORED",
                    changeSummary: "恢复历史版本 v1"
                }
            ]);
        }
        if (path.endsWith("/classics/ming-customs/versions/get")) {
            return apiResponse({
                id: "9001",
                contentType: "MING_CUSTOMS",
                contentId: "500000000001",
                versionNo: 1,
                versionedAt: "2026-01-01T00:00:00.000+00:00",
                snapshotJson: JSON.stringify({
                    title: "旧标题",
                    category: "RITUAL",
                    chapter: "岁时礼仪",
                    section: "正旦",
                    summary: "旧版摘要",
                    contentFormat: "MARKDOWN",
                    content: "## 旧版",
                    originalExcerpts: "旧版摘录",
                    visibility: "PUBLIC"
                }),
                changeType: "HISTORY_RESTORED",
                changeSummary: "恢复历史版本 v1"
            });
        }
        if (path.endsWith("/classics/ming-customs/versions/reset")) {
            const body = readFetchBody(init?.body) as { id: string };
            return apiResponse({
                id: "9002",
                contentType: "MING_CUSTOMS",
                contentId: body.id,
                versionNo: 2,
                versionedAt: "2026-01-02T00:00:00.000+00:00",
                snapshotJson: JSON.stringify({
                    title: "新标题",
                    category: "RITUAL",
                    chapter: "岁时礼仪",
                    section: "正旦",
                    summary: "恢复后的摘要",
                    contentFormat: "MARKDOWN",
                    content: "## 恢复正文",
                    originalExcerpts: "恢复后摘录",
                    visibility: "PUBLIC"
                }),
                changeType: "HISTORY_RESTORED",
                changeSummary: "恢复历史版本 v1"
            });
        }
        if (path.endsWith("/classics/shares/batch/create")) {
            return apiResponse({
                failureCount: 1,
                failures: [
                    {
                        contentId: "500000000002",
                        contentType: "MING_CUSTOMS",
                        failureCode: "CONTENT_NOT_FOUND",
                        failureReason: "习俗条目不存在",
                        status: "FAILED"
                    }
                ],
                successCount: 1,
                successes: [
                    {
                        contentId: "500000000001",
                        contentType: "MING_CUSTOMS",
                        resultId: "9201",
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
                        contentId: "500000000002",
                        contentType: "MING_CUSTOMS",
                        failureCode: "BATCH_VISIBILITY_FAILED",
                        failureReason: "习俗条目不存在",
                        status: "FAILED"
                    }
                ],
                successCount: 1,
                successes: [
                    {
                        contentId: "500000000001",
                        contentType: "MING_CUSTOMS",
                        resultId: "500000000001",
                        status: "PUBLIC"
                    }
                ]
            });
        }
        if (path.endsWith("/ai/invocation/candidate/list")) {
            return apiResponse([
                {
                    candidateId: "6001",
                    contentType: "MING_CUSTOMS",
                    contentId: "500000000001",
                    capability: "summary",
                    objectId: null,
                    resultFormat: "TEXT",
                    resultPayload: "文献摘要候选",
                    status: "PENDING",
                    requestedAt: "2026-01-01T00:00:00.000+00:00"
                }
            ]);
        }
        if (path.endsWith("/classics/content/ai-candidates/batch/apply")) {
            return apiResponse({
                failureCount: 1,
                failures: [
                    {
                        candidateId: "6002",
                        contentType: "MING_CUSTOMS",
                        contentId: "500000000002",
                        capability: "summary",
                        failureCode: "INVALID_FORMAT",
                        failureReason: "payload invalid"
                    }
                ],
                successCount: 1,
                successes: [
                    {
                        candidateId: "6001",
                        contentType: "MING_CUSTOMS",
                        contentId: "500000000001",
                        capability: "summary",
                        objectId: null,
                        resultId: "5001",
                        status: "APPLIED"
                    }
                ]
            });
        }

        return apiResponse(true);
    });
};

describe("MingCustomsPage", () => {
    let queryClient: QueryClient;

    beforeEach(() => {
        vi.clearAllMocks();
        queryClient = createTestQueryClient();
        capturedCalls.length = 0;
        mockMingCustomsRecord = {
            id: "500000000001",
            title: "岁时礼仪：元旦朝贺",
            category: "RITUAL",
            chapter: "岁时礼仪",
            section: "正旦",
            summary: "记录明代正旦朝贺与家族拜礼。",
            contentFormat: "MARKDOWN",
            content: "## 正旦",
            originalExcerpts: "正旦朝贺。",
            visibility: "PUBLIC"
        };
        confirmDangerMock.mockClear();
        confirmDangerMock.mockImplementation((options: { onConfirm?: () => void }) =>
            options.onConfirm?.()
        );
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        replacePermissions([
            "classics:mingcustoms:view",
            "classics:mingcustoms:edit",
            "classics:sharing:edit",
            "classics:content:export"
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

    it("renders page and first record", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <MingCustomsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "明代习俗" })).toBeInTheDocument();
        expect(await screen.findByText("岁时礼仪：元旦朝贺")).toBeInTheDocument();
    }, 30000);

    it("opens export jobs from a large drawer action", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <MingCustomsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await screen.findByText("岁时礼仪：元旦朝贺");
        expect(
            screen.queryByTestId("classics-ming-customs-ming-customs-export-jobs-drawer")
        ).not.toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "打开明代习俗导出任务" }));

        const drawer = await screen.findByTestId(
            "classics-ming-customs-ming-customs-export-jobs-drawer"
        );
        expect(drawer).toBeInTheDocument();
        expect(document.querySelector(".kuzhambu-drawer-large")).toBeTruthy();
        expect(document.querySelector('[aria-label="明代习俗导出任务"]')).toBeTruthy();
    }, 30000);

    it("filters list by tag cloud selection and clears selected tag filter", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <MingCustomsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await screen.findByText("岁时礼仪：元旦朝贺");
        capturedCalls.length = 0;

        await user.click(screen.getByRole("button", { name: /标签云/ }));
        await user.click(await screen.findByRole("button", { name: "筛选标签 礼制，3 条" }));

        await waitFor(() => {
            expect(
                capturedCalls.some(
                    (call) =>
                        call.method === "POST" &&
                        call.path === "/classics/ming-customs/page" &&
                        (
                            call.body as {
                                tagId?: string;
                                tagNameSnapshot?: string;
                                tagName?: string;
                            }
                        ).tagId === "700000000001" &&
                        (
                            call.body as {
                                tagId?: string;
                                tagNameSnapshot?: string;
                                tagName?: string;
                            }
                        ).tagNameSnapshot === "礼制" &&
                        !("tagName" in (call.body as Record<string, unknown>))
                )
            ).toBeTruthy();
        });
        expect(await screen.findByText(/当前标签筛选：礼制/)).toBeInTheDocument();

        capturedCalls.length = 0;
        await user.click(screen.getByRole("button", { name: "清除标签筛选" }));

        await waitFor(() => {
            expect(
                capturedCalls.some((call) => {
                    const body = call.body as Record<string, unknown>;
                    return (
                        call.method === "POST" &&
                        call.path === "/classics/ming-customs/page" &&
                        !("tagId" in body) &&
                        !("tagNameSnapshot" in body) &&
                        !("tagName" in body)
                    );
                })
            ).toBeTruthy();
        });
        expect(screen.queryByText(/当前标签筛选：礼制/)).not.toBeInTheDocument();
    }, 30000);

    it("creates summary tags and qa refinement tasks from the entry drawer", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <MingCustomsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("ming-customs-edit-500000000001-button"));
        await user.click(await screen.findByRole("button", { name: "创建摘要任务" }));
        await waitFor(() => expect(aiRefinementTaskService.createTask).toHaveBeenCalledTimes(1));
        await user.click(await screen.findByRole("button", { name: "创建标签任务" }));
        await waitFor(() => expect(aiRefinementTaskService.createTask).toHaveBeenCalledTimes(2));
        await user.click(await screen.findByRole("button", { name: "创建问答任务" }));
        await waitFor(() => expect(aiRefinementTaskService.createTask).toHaveBeenCalledTimes(3));

        const calls = vi
            .mocked(aiRefinementTaskService.createTask)
            .mock.calls.map(([payload]) => payload);
        expect(calls.map((payload) => payload.capability)).toEqual(["summary", "tags", "qa"]);
        calls.forEach((payload) => {
            expect(payload).toEqual(
                expect.objectContaining({
                    scope: "classics",
                    contentType: "MING_CUSTOMS",
                    contentId: "500000000001",
                    serviceRole: "PRIMARY",
                    modelId: "1",
                    modelName: "gpt-5.5",
                    locale: "zh-CN"
                })
            );
            expect(payload).not.toHaveProperty("requestedBy");
            expect(payload.requestId).toContain(`ming-customs-${payload.capability}-request`);
            expect(payload.traceId).toContain(`ming-customs-${payload.capability}-trace`);
        });
    }, 30000);

    it("does not create refinement task when entry text is empty", async () => {
        const user = userEvent.setup();
        mockMingCustomsRecord = {
            ...mockMingCustomsRecord,
            content: "",
            originalExcerpts: ""
        };

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <MingCustomsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("ming-customs-edit-500000000001-button"));
        await user.click(await screen.findByRole("button", { name: "创建问答任务" }));

        expect(aiRefinementTaskService.createTask).not.toHaveBeenCalled();
        expect(
            await screen.findByText("正文与原文摘录均为空，无法创建 AI 精修任务")
        ).toBeInTheDocument();
    }, 30000);

    it("creates batch shares from selected entries and shows item failures", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <MingCustomsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        const table = await screen.findByLabelText("明代习俗表格");
        await waitForSelectableRow(table);
        expect(screen.getByText("当前页已选 0 条")).toBeInTheDocument();
        const batchShareButton = screen.getByTestId(
            "classics-ming-customs-ming-customs-batch-share-button"
        );
        selectFirstRow(table);
        await waitFor(() => {
            expect(batchShareButton).not.toBeDisabled();
            expect(screen.getByText("当前页已选 1 条")).toBeInTheDocument();
        });
        await user.click(batchShareButton);

        await waitFor(() => {
            expect(screen.getByText("批量分享结果：成功 1，失败 1")).toBeInTheDocument();
        });
        expect(screen.getByText("MING_CUSTOMS#500000000002: 习俗条目不存在")).toBeInTheDocument();
    }, 30000);

    it("changes selected entries visibility and shows item failures", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <MingCustomsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        const table = await screen.findByLabelText("明代习俗表格");
        await waitForSelectableRow(table);
        const batchPublicButton = screen.getByTestId(
            "classics-ming-customs-ming-customs-batch-public-button"
        );
        selectFirstRow(table);
        await waitFor(() => {
            expect(batchPublicButton).not.toBeDisabled();
        });
        await user.click(batchPublicButton);

        await waitFor(() => {
            expect(screen.getByText("批量可见性结果：成功 1，失败 1")).toBeInTheDocument();
        });
        expect(capturedCalls).toContainEqual({
            body: {
                contentIds: ["500000000001"],
                contentType: "MING_CUSTOMS",
                visibility: "PUBLIC"
            },
            method: "POST",
            path: "/classics/content/visibility/change"
        });
        expect(screen.getByText("MING_CUSTOMS#500000000002: 习俗条目不存在")).toBeInTheDocument();
    }, 30000);

    it("opens batch candidate governance drawer from selected entries", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <MingCustomsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        const table = await screen.findByLabelText("明代习俗表格");
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
        const candidateCheckbox = within(candidateTable).getByRole("checkbox", {
            name: /Select row/
        });
        expect(candidateCheckbox).toBeInTheDocument();
        expect(
            capturedCalls.some(
                (call) =>
                    call.path === "/ai/invocation/candidate/list" &&
                    call.method === "POST" &&
                    (call.body as { contentType: string; contentId: string; status: string })
                        .contentId === "500000000001"
            )
        ).toBeTruthy();

        await user.click(candidateCheckbox);
        await user.click(screen.getByRole("button", { name: "批量应用" }));

        await waitFor(() => {
            expect(
                screen.queryAllByText("批量候选应用结果：成功 1，失败 1").length
            ).toBeGreaterThanOrEqual(1);
        });
        expect(screen.getByText("6002 / summary / payload invalid")).toBeInTheDocument();
        expect(capturedCalls).toContainEqual({
            body: {
                items: [
                    {
                        candidateId: "6001",
                        contentType: "MING_CUSTOMS",
                        contentId: "500000000001",
                        capability: "summary",
                        objectId: null,
                        resultFormat: "TEXT",
                        resultPayload: "文献摘要候选",
                        changeSummary: "AI 应用：summary"
                    }
                ]
            },
            method: "POST",
            path: "/classics/content/ai-candidates/batch/apply"
        });
    }, 30000);

    it("refreshes detail and tags/qa/versions after ai candidate apply", async () => {
        const user = userEvent.setup();
        const invalidateSpy = vi.spyOn(queryClient, "invalidateQueries");

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <MingCustomsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("ming-customs-edit-500000000001-button"));
        capturedCalls.length = 0;
        await user.click(await screen.findByRole("button", { name: "mock-ai-applied" }));

        await waitFor(() => {
            expect(
                invalidateSpy.mock.calls.some(
                    ([arg]) =>
                        JSON.stringify(arg?.queryKey) === JSON.stringify(["ming-customs", "detail"])
                )
            ).toBeTruthy();
            expect(
                invalidateSpy.mock.calls.some(
                    ([arg]) =>
                        JSON.stringify(arg?.queryKey) ===
                        JSON.stringify(["classics", "content", "tags", "MING_CUSTOMS"])
                )
            ).toBeTruthy();
            expect(
                invalidateSpy.mock.calls.some(
                    ([arg]) =>
                        JSON.stringify(arg?.queryKey) ===
                        JSON.stringify(["classics", "content", "qa-pairs", "MING_CUSTOMS"])
                )
            ).toBeTruthy();
            expect(
                invalidateSpy.mock.calls.some(
                    ([arg]) =>
                        JSON.stringify(arg?.queryKey) ===
                        JSON.stringify(["ming-customs", "versions"])
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
                    <MingCustomsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("ming-customs-edit-500000000001-button"));
        capturedCalls.length = 0;
        await user.click(await screen.findByRole("button", { name: "mock-ai-rejected" }));

        await waitFor(() => {
            expect(
                invalidateSpy.mock.calls.some(
                    ([arg]) =>
                        JSON.stringify(arg?.queryKey) ===
                        JSON.stringify(["ai", "candidates", "MING_CUSTOMS", "500000000001"])
                )
            ).toBeTruthy();
        });

        expect(
            invalidateSpy.mock.calls.every(
                ([arg]) =>
                    JSON.stringify(arg?.queryKey) ===
                    JSON.stringify(["ai", "candidates", "MING_CUSTOMS", "500000000001"])
            )
        ).toBeTruthy();
    }, 30000);

    it("renders ming customs version history panel with snapshot compare", () => {
        const version: MingCustomsContentVersionRecord = {
            id: "500000000003",
            versionNo: 12,
            changeType: "HISTORY_RESTORED",
            changeSummary: "恢复历史版本 v12",
            versionedAt: "2026-06-01T00:00:00.000+00:00",
            snapshotJson: JSON.stringify({
                title: "旧标题",
                category: "RITUAL",
                chapter: "先秦",
                section: "开端",
                summary: "旧版摘要",
                contentFormat: "MARKDOWN",
                content: "旧版正文",
                originalExcerpts: "旧版摘录",
                visibility: "PUBLIC",
                tags: [
                    {
                        id: "9001",
                        tagId: "11001",
                        tagNameSnapshot: "礼制"
                    },
                    {
                        id: "9002",
                        tagId: "11002",
                        tagNameSnapshot: "朝仪"
                    }
                ],
                qaPairs: [
                    {
                        id: "8001",
                        question: "问：元旦朝贺是什么？",
                        answer: "元旦朝贺是祭祖和祭社的重要礼节。"
                    },
                    {
                        id: "8002",
                        question: "问：参与者有哪些？",
                        answer: "家族长辈与主祭人。"
                    }
                ]
            })
        };

        render(
            <MingCustomsVersionHistoryPanel
                currentEntry={{
                    id: "500000000001",
                    title: "新标题",
                    category: "RITUAL",
                    chapter: "先秦",
                    section: "开端",
                    summary: "新版摘要",
                    contentFormat: "MARKDOWN",
                    content: "新版正文",
                    originalExcerpts: "新版摘录",
                    visibility: "PRIVATE"
                }}
                detailLoading={false}
                listLoading={false}
                selectedVersion={version}
                versions={[version]}
                onResetVersion={vi.fn()}
                onSelectVersion={vi.fn()}
            />
        );

        expect(screen.getByLabelText("明代习俗版本历史面板")).toBeInTheDocument();
        expect(
            screen.getByTestId("ming-customs-version-view-500000000003-button")
        ).toBeInTheDocument();
        expect(
            screen.getByTestId("ming-customs-version-restore-500000000003-button")
        ).toBeInTheDocument();
        expect(screen.getByText(/历史：旧版摘要/)).toBeInTheDocument();
        expect(screen.getByText(/当前：新版摘要/)).toBeInTheDocument();
        expect(screen.getByText("确认标签")).toBeInTheDocument();
        expect(screen.getByText("礼制")).toBeInTheDocument();
        expect(screen.getByText("朝仪")).toBeInTheDocument();
        expect(
            screen.getByText("Q: 问：元旦朝贺是什么？；A: 元旦朝贺是祭祖和祭社的重要礼节。")
        ).toBeInTheDocument();
        expect(
            screen.getByText("Q: 问：参与者有哪些？；A: 家族长辈与主祭人。")
        ).toBeInTheDocument();
    });

    it("disables reset when ming customs version snapshot invalid", () => {
        const version: MingCustomsContentVersionRecord = {
            id: "500000000004",
            versionNo: 13,
            changeType: "HISTORY_RESTORED",
            changeSummary: "恢复历史版本 v13",
            versionedAt: "2026-06-01T00:00:00.000+00:00",
            snapshotJson: "{bad-json"
        };

        render(
            <MingCustomsVersionHistoryPanel
                currentEntry={null}
                selectedVersion={version}
                versions={[version]}
                onResetVersion={vi.fn()}
                onSelectVersion={vi.fn()}
            />
        );

        expect(screen.getByText("版本快照为空或无法解析")).toBeInTheDocument();
        expect(
            screen.getByTestId("ming-customs-version-restore-500000000004-button")
        ).toBeDisabled();
    });

    it("shows empty state when version history is empty", () => {
        render(
            <MingCustomsVersionHistoryPanel
                currentEntry={{
                    id: "500000000001",
                    title: "版本空白测试条目"
                }}
                selectedVersion={null}
                versions={[]}
                onResetVersion={vi.fn()}
                onSelectVersion={vi.fn()}
            />
        );

        expect(screen.getByText("暂无版本历史")).toBeInTheDocument();
    });

    it("shows version history in edit mode and displays compare details", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <MingCustomsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("ming-customs-edit-500000000001-button"));
        expect(await screen.findByLabelText("明代习俗版本历史面板")).toBeInTheDocument();
        await user.click(await screen.findByTestId("ming-customs-version-view-9001-button"));

        expect(await screen.findByText("当前：岁时礼仪：元旦朝贺")).toBeInTheDocument();
        expect(screen.getByText("历史：旧标题")).toBeInTheDocument();
    });

    it("shows reset confirm and calls versions/reset for selected history version", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <MingCustomsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByTestId("ming-customs-edit-500000000001-button"));
        await user.click(await screen.findByTestId("ming-customs-version-view-9001-button"));
        await user.click(await screen.findByTestId("ming-customs-version-restore-9001-button"));

        await waitFor(() => {
            expect(confirmDangerMock).toHaveBeenCalledWith(
                expect.objectContaining({
                    message: "恢复后会生成新的正式版本，当前内容将被历史版本覆盖。",
                    title: "确认恢复明代习俗历史版本"
                })
            );
            expect(capturedCalls).toContainEqual({
                body: { id: "500000000001", versionId: "9001" },
                method: "POST",
                path: "/classics/ming-customs/versions/reset"
            });
        });
    });
});
