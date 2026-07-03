import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as currentUserService from "@/service/current-user-service";
import { MingCustomsPage } from "./ming-customs-page";

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

const readFetchUrl = (input: RequestInfo | URL) => {
    if (typeof input === "string") {
        return input;
    }
    if (input instanceof URL) {
        return input.href;
    }
    return input.url;
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
    vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
        const path = readFetchUrl(input).replace("/kuzhambu-admin-api/api", "");

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
        if (path.endsWith("/classics/shares/batch-create")) {
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

        return apiResponse(true);
    });
};

describe("MingCustomsPage", () => {
    let queryClient: QueryClient;

    beforeEach(() => {
        queryClient = createTestQueryClient();
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        installFetchMock();
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
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
        const batchShareButton = screen.getByRole("button", { name: "批量分享" });
        await user.click(within(table).getByRole("checkbox", { name: "Select all" }));
        await waitFor(() => {
            expect(batchShareButton).not.toBeDisabled();
        });
        await user.click(batchShareButton);

        await waitFor(() => {
            expect(screen.getByText("批量分享结果：成功 1，失败 1")).toBeInTheDocument();
        });
        expect(screen.getByText("MING_CUSTOMS#500000000002: 习俗条目不存在")).toBeInTheDocument();
    }, 30000);
});
