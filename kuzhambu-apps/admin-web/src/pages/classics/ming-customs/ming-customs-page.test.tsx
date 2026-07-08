import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { clearPermissions, replacePermissions } from "@/auth/permission-storage";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as currentUserService from "@/service/current-user-service";
import { MingCustomsVersionHistoryPanel } from "./components/ming-customs-version-history-panel";
import { MingCustomsPage } from "./ming-customs-page";
import type { MingCustomsContentVersionRecord } from "./ming-customs-types";

vi.mock("@/service/current-user-service", () => ({
    getCurrentUserInfo: vi.fn(() => Promise.resolve({ id: 99, loginName: "admin", name: "Admin" }))
}));

vi.mock("@/pages/classics/common/ai-refinement-task-service", () => ({
    createTask: vi.fn(() =>
        Promise.resolve({ id: 9101, status: "PENDING", capability: "summary" })
    ),
    getTask: vi.fn(),
    pageTasks: vi.fn(() => Promise.resolve({ items: [], totalCount: 0, pageNo: 1, pageSize: 10 })),
    cancelTask: vi.fn()
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
                records: [
                    {
                        id: 500000000001,
                        title: "岁时礼仪：元旦朝贺",
                        category: "RITUAL",
                        chapter: "岁时礼仪",
                        section: "正旦",
                        summary: "记录明代正旦朝贺与家族拜礼。",
                        contentFormat: "MARKDOWN",
                        content: "## 正旦",
                        originalExcerpts: "正旦朝贺。",
                        visibility: "PUBLIC"
                    }
                ]
            });
        }

        if (path.endsWith("/classics/ming-customs/keyword-cloud")) {
            return apiResponse([]);
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
        if (path.endsWith("/classics/ming-customs/500000000001")) {
            return apiResponse({
                id: 500000000001,
                title: "岁时礼仪：元旦朝贺",
                category: "RITUAL",
                chapter: "岁时礼仪",
                section: "正旦",
                summary: "记录明代正旦朝贺与家族拜礼。",
                contentFormat: "MARKDOWN",
                content: "## 正旦",
                originalExcerpts: "正旦朝贺。",
                visibility: "PUBLIC"
            });
        }
        if (path.endsWith("/classics/shares/batch/create")) {
            return apiResponse({
                failureCount: 1,
                failures: [
                    {
                        contentId: 500000000002,
                        contentType: "MING_CUSTOMS",
                        failureCode: "CONTENT_NOT_FOUND",
                        failureReason: "习俗条目不存在",
                        status: "FAILED"
                    }
                ],
                successCount: 1,
                successes: [
                    {
                        contentId: 500000000001,
                        contentType: "MING_CUSTOMS",
                        resultId: 9201,
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
                        contentId: 500000000002,
                        contentType: "MING_CUSTOMS",
                        failureCode: "BATCH_VISIBILITY_FAILED",
                        failureReason: "习俗条目不存在",
                        status: "FAILED"
                    }
                ],
                successCount: 1,
                successes: [
                    {
                        contentId: 500000000001,
                        contentType: "MING_CUSTOMS",
                        resultId: 500000000001,
                        status: "PUBLIC"
                    }
                ]
            });
        }
        if (path.endsWith("/ai/invocation/candidate/list")) {
            return apiResponse([
                {
                    candidateId: 6001,
                    contentType: "MING_CUSTOMS",
                    contentId: 500000000001,
                    capability: "summary",
                    objectId: null,
                    resultFormat: "TEXT",
                    resultPayload: "文献摘要候选",
                    status: "PENDING",
                    requestedAt: "2026-01-01T00:00:00.000+00:00"
                }
            ]);
        }
        if (path.endsWith("/classics/content/ai-candidates/batch/change")) {
            return apiResponse({
                failureCount: 1,
                failures: [
                    {
                        candidateId: 6002,
                        contentType: "MING_CUSTOMS",
                        contentId: 500000000002,
                        capability: "summary",
                        failureCode: "INVALID_FORMAT",
                        failureReason: "payload invalid"
                    }
                ],
                successCount: 1,
                successes: [
                    {
                        candidateId: 6001,
                        contentType: "MING_CUSTOMS",
                        contentId: 500000000001,
                        capability: "summary",
                        objectId: null,
                        resultId: 5001,
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
        queryClient = createTestQueryClient();
        capturedCalls.length = 0;
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

    it("creates summary refinement task from the entry drawer", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <MingCustomsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(
            await screen.findByRole("button", { name: "编辑明代习俗 岁时礼仪：元旦朝贺" })
        );
        await user.click(await screen.findByRole("button", { name: "创建摘要任务" }));

        expect(currentUserService.getCurrentUserInfo).toHaveBeenCalled();
        expect(aiRefinementTaskService.createTask).toHaveBeenCalled();
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).toEqual(
            expect.objectContaining({
                capability: "summary",
                scope: "classics",
                contentType: "MING_CUSTOMS",
                contentId: 500000000001,
                requestedBy: 99,
                serviceRole: "PRIMARY",
                modelId: 1,
                modelName: "gpt-5.5",
                locale: "zh-CN"
            })
        );
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
        const batchShareButton = screen.getByRole("button", { name: "批量分享" });
        selectFirstRow(table);
        await waitFor(() => {
            expect(batchShareButton).not.toBeDisabled();
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
        const batchPublicButton = screen.getByRole("button", { name: "批量公开" });
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
                contentIds: [500000000001],
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
        const candidateCheckbox = within(candidateTable).getByRole("checkbox", {
            name: /Select row/
        });
        expect(candidateCheckbox).toBeInTheDocument();
        expect(
            capturedCalls.some(
                (call) =>
                    call.path === "/ai/invocation/candidate/list" &&
                    call.method === "POST" &&
                    (call.body as { contentType: string; contentId: number; status: string })
                        .contentId === 500000000001
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
                        candidateId: 6001,
                        contentType: "MING_CUSTOMS",
                        contentId: 500000000001,
                        capability: "summary",
                        objectId: null,
                        resultFormat: "TEXT",
                        resultPayload: "文献摘要候选",
                        changeSummary: "AI 应用：summary"
                    }
                ]
            },
            method: "POST",
            path: "/classics/content/ai-candidates/batch/change"
        });
    }, 30000);

    it("renders ming customs version history panel with snapshot compare", () => {
        const version: MingCustomsContentVersionRecord = {
            id: 500000000003,
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
                visibility: "PUBLIC"
            })
        };

        render(
            <MingCustomsVersionHistoryPanel
                currentEntry={{
                    id: 500000000001,
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
        expect(screen.getByRole("button", { name: "查看明代习俗版本 12" })).toBeInTheDocument();
        expect(screen.getByText("恢复明代习俗版本 12")).toBeInTheDocument();
        expect(screen.getByText(/历史：旧版摘要/)).toBeInTheDocument();
        expect(screen.getByText(/当前：新版摘要/)).toBeInTheDocument();
    });

    it("disables reset when ming customs version snapshot invalid", () => {
        const version: MingCustomsContentVersionRecord = {
            id: 500000000004,
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
        expect(screen.getByRole("button", { name: "恢复明代习俗版本 13" })).toBeDisabled();
    });
});
