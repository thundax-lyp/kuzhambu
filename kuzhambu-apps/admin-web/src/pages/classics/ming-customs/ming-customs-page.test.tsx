import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { MingCustomsPage } from "./ming-customs-page";

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

        return apiResponse(true);
    });
};

describe("MingCustomsPage", () => {
    beforeEach(() => {
        queryClient.clear();
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
    }, 10000);
});
