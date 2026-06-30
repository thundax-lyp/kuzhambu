import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { SancaiPage } from "./sancai-page";

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

        if (path.endsWith("/classics/sancai/categories/list")) {
            return apiResponse([{ categoryType: "FORMAL", id: 2, title: "天文" }]);
        }
        if (path.endsWith("/classics/sancai/categories/types")) {
            return apiResponse([
                { label: "正式门类", type: "SANCAI_CATEGORY_TYPE", value: "FORMAL" }
            ]);
        }
        if (path.endsWith("/classics/sancai/volumes/types")) {
            return apiResponse([{ label: "正式卷目", type: "SANCAI_VOLUME_TYPE", value: "MAIN" }]);
        }
        if (path.endsWith("/classics/sancai/volumes/list")) {
            return apiResponse([{ categoryId: 2, id: 101, title: "天文卷一", volumeType: "MAIN" }]);
        }
        if (path.endsWith("/classics/sancai/entries/page")) {
            return apiResponse({
                pageNo: 1,
                pageSize: 20,
                totalCount: 0,
                totalPage: 0,
                records: []
            });
        }
        if (path.endsWith("/classics/sancai/entries/list")) {
            return apiResponse([]);
        }

        return apiResponse(true);
    });
};

describe("SancaiPage", () => {
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

    it("renders page and category tree content", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SancaiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "三才图会" })).toBeInTheDocument();
        expect(await screen.findByRole("link", { name: "打开门类 天文" })).toBeInTheDocument();
    }, 10000);
});
