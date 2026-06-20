import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { MingCustomsPage } from "./ming-customs-page";

interface CapturedCall {
    body: unknown;
    method: string | undefined;
    path: string;
}

const capturedCalls: CapturedCall[] = [];

const apiResponse = (data: unknown) =>
    Promise.resolve(
        new Response(
            JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data
            }),
            {
                headers: {
                    "Content-Type": "application/json"
                },
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

const installMingCustomsFetchMock = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
        const url = readFetchUrl(input);
        const path = url.replace("/admin-api/api", "");
        capturedCalls.push({
            body: init?.body ? JSON.parse(String(init.body)) : undefined,
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
                        visibility: "PUBLIC"
                    }
                ]
            });
        }

        if (path.endsWith("/classics/ming-customs/keyword-cloud")) {
            return apiResponse([
                { keyword: "礼制", count: 8 },
                { keyword: "正旦", count: 2 }
            ]);
        }

        if (path.endsWith("/sys/dict/page")) {
            return apiResponse({
                pageNo: 1,
                pageSize: 100,
                totalCount: 2,
                count: 2,
                totalPage: 1,
                records: [
                    {
                        type: "CLASSICS_MING_CUSTOMS_CATEGORY",
                        value: "RITUAL",
                        label: "礼制"
                    },
                    {
                        type: "CLASSICS_MING_CUSTOMS_CATEGORY",
                        value: "FESTIVAL",
                        label: "岁时节令"
                    }
                ]
            });
        }

        return apiResponse(true);
    });
};

const renderMingCustomsPage = () => {
    render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <MingCustomsPage />
            </AntdApp>
        </QueryClientProvider>
    );
};

describe("MingCustomsPage", () => {
    beforeEach(() => {
        capturedCalls.length = 0;
        queryClient.clear();
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        installMingCustomsFetchMock();
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        localStorage.clear();
        vi.restoreAllMocks();
    });

    it("shows page title and loads the first page", async () => {
        renderMingCustomsPage();

        expect(await screen.findByRole("heading", { name: "明代习俗" })).toBeInTheDocument();
        expect(await screen.findByText("岁时礼仪：元旦朝贺")).toBeInTheDocument();
        await waitFor(() => {
            expect(capturedCalls).toContainEqual({
                method: "POST",
                path: "/classics/ming-customs/page",
                body: {
                    pageNo: 1,
                    pageSize: 20,
                    sortDirection: "DESC"
                }
            });
        });
    });

    it("filters by category and keyword cloud", async () => {
        const user = userEvent.setup();
        renderMingCustomsPage();

        await screen.findByText("岁时礼仪：元旦朝贺");
        await user.click(screen.getByRole("button", { name: "filter 筛选" }));
        await user.click(screen.getByLabelText("明代习俗分类"));
        await user.click(await screen.findByTitle("礼制"));
        await user.click(screen.getByRole("button", { name: /查\s*询/ }));

        await waitFor(() => {
            expect(capturedCalls).toContainEqual({
                method: "POST",
                path: "/classics/ming-customs/page",
                body: {
                    pageNo: 1,
                    pageSize: 20,
                    category: "RITUAL",
                    sortDirection: "DESC"
                }
            });
        });

        const cloud = await screen.findByLabelText("明代习俗关键词云");
        expect(within(cloud).getByText("8")).toBeInTheDocument();
        await user.click(within(cloud).getByRole("button", { name: "筛选关键词 礼制，8 次" }));

        await waitFor(() => {
            expect(capturedCalls).toContainEqual({
                method: "POST",
                path: "/classics/ming-customs/page",
                body: {
                    pageNo: 1,
                    pageSize: 20,
                    keyword: "礼制",
                    category: "RITUAL",
                    sortDirection: "DESC"
                }
            });
        });
    });
});
