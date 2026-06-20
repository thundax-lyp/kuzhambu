import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { SancaiPage } from "./sancai-page";

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

const installSancaiFetchMock = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
        const url = readFetchUrl(input);
        const path = url.replace("/admin-api/api", "");
        capturedCalls.push({
            body: init?.body ? JSON.parse(String(init.body)) : undefined,
            method: init?.method,
            path
        });

        if (path.endsWith("/classics/sancai/categories/list")) {
            return apiResponse([
                {
                    categoryType: "FORMAL",
                    id: 2,
                    title: "天文"
                }
            ]);
        }

        if (path.endsWith("/classics/sancai/categories/types")) {
            return apiResponse([
                { label: "正式门类", type: "SANCAI_CATEGORY_TYPE", value: "FORMAL" },
                { label: "辅助内容", type: "SANCAI_CATEGORY_TYPE", value: "AUXILIARY" }
            ]);
        }

        if (path.endsWith("/classics/sancai/volumes/types")) {
            return apiResponse([
                { label: "正式卷目", type: "SANCAI_VOLUME_TYPE", value: "MAIN" },
                { label: "辅助卷目", type: "SANCAI_VOLUME_TYPE", value: "AUXILIARY" }
            ]);
        }

        if (path.endsWith("/classics/sancai/volumes/list")) {
            return apiResponse([
                {
                    categoryId: 2,
                    id: 101,
                    title: "天文卷一",
                    volumeType: "MAIN"
                },
                {
                    categoryId: 2,
                    id: 102,
                    title: "天文卷二",
                    volumeType: "MAIN"
                }
            ]);
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
            return apiResponse([
                {
                    id: 3001,
                    volumeId: 101,
                    title: "天地",
                    originalText: "天地玄黄",
                    translationText: "译文",
                    summary: "天地摘要",
                    lifecycleStatus: "DRAFT",
                    visibility: "PUBLIC",
                    translationStatus: "PENDING",
                    imageStatus: "PENDING",
                    visualAssetStatus: "PENDING",
                    refinementStatus: "PENDING"
                }
            ]);
        }
        if (path.endsWith("/classics/sancai/volumes/add")) {
            return apiResponse({
                categoryId: 2,
                id: 102,
                title: "新卷",
                volumeType: "MAIN"
            });
        }

        if (path.endsWith("/classics/sancai/volumes/update")) {
            return apiResponse({
                categoryId: 2,
                id: 101,
                title: "天文卷一修订",
                volumeType: "MAIN"
            });
        }

        if (path.endsWith("/classics/sancai/volumes/delete")) {
            return apiResponse(true);
        }

        if (path.endsWith("/classics/sancai/volumes/sort")) {
            return apiResponse(true);
        }

        return apiResponse(true);
    });
};

const renderSancaiPage = () => {
    render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <SancaiPage />
            </AntdApp>
        </QueryClientProvider>
    );
};

const selectCategoryFromTable = async (user: ReturnType<typeof userEvent.setup>, title: string) => {
    const categoryTable = await screen.findByLabelText("三才图会门类表格");
    await user.click(await within(categoryTable).findByRole("link", { name: `打开门类 ${title}` }));
};

describe("SancaiPage volume CRUD", () => {
    beforeEach(() => {
        capturedCalls.length = 0;
        queryClient.clear();
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        installSancaiFetchMock();
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        localStorage.clear();
        vi.restoreAllMocks();
    });

    it("shows category panel when selecting the catalog root", async () => {
        const user = userEvent.setup();

        renderSancaiPage();

        await selectCategoryFromTable(user, "天文");
        expect(await screen.findByRole("link", { name: "打开卷目 天文卷一" })).toBeInTheDocument();

        const catalogTree = screen.getByLabelText("三才图会目录树");
        await user.click(within(catalogTree).getByText("三才图会"));

        expect(await screen.findByLabelText("三才图会门类表格")).toBeInTheDocument();
    }, 10000);
});
