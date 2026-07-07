import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { clearPermissions, replacePermissions } from "@/auth/permission-storage";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as currentUserService from "@/service/current-user-service";
import { WangqiPage } from "./wangqi-page";

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
                records: [
                    {
                        id: 400000000001,
                        title: "王圻文档",
                        summary: "记录王圻古籍条目。",
                        contentFormat: "MARKDOWN",
                        content: "## 王圻",
                        documentTime: "2026-01-01T00:00:00.000+00:00",
                        storageObjectId: 7001,
                        visibility: "PUBLIC"
                    }
                ]
            });
        }
        if (path.endsWith("/classics/wangqi/documents/400000000001/get")) {
            return apiResponse({
                id: 400000000001,
                title: "王圻文档",
                summary: "记录王圻古籍条目。",
                contentFormat: "MARKDOWN",
                content: "## 王圻",
                documentTime: "2026-01-01T00:00:00.000+00:00",
                storageObjectId: 7001,
                visibility: "PUBLIC"
            });
        }
        if (path.endsWith("/classics/wangqi/documents/timeline/list")) {
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
        queryClient = createTestQueryClient();
        capturedCalls.length = 0;
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        replacePermissions([
            "classics:wangqi:view",
            "classics:wangqi:edit",
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

    it("creates summary refinement task from the document drawer", async () => {
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

        expect(currentUserService.getCurrentUserInfo).toHaveBeenCalled();
        expect(aiRefinementTaskService.createTask).toHaveBeenCalled();
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).toEqual(
            expect.objectContaining({
                capability: "summary",
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
});
