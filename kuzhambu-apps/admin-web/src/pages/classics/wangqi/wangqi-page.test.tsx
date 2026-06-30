import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { WangqiPage } from "./wangqi-page";

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({
        danger: vi.fn()
    })
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
        if (path.endsWith("/classics/wangqi/documents/timeline/list")) {
            return apiResponse([]);
        }
        return apiResponse(true);
    });
};

describe("WangqiPage", () => {
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
});
