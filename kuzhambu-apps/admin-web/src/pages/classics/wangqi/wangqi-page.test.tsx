import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as currentUserService from "@/service/current-user-service";
import { WangqiPage } from "./wangqi-page";

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({
        danger: vi.fn()
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
        return apiResponse(true);
    });
};

describe("WangqiPage", () => {
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
    }, 10000);

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
    }, 10000);
});
