import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { SancaiContentPanel } from "./components/sancai-content-panel";
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

        if (path.endsWith("/classics/sancai/contents/list")) {
            return apiResponse([
                {
                    answer: "天地玄黄",
                    entryId: 3001,
                    id: 9001,
                    question: "何为天地"
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

        if (path.endsWith("/classics/sancai/contents/add")) {
            return apiResponse({
                id: 9002
            });
        }

        if (path.endsWith("/classics/sancai/contents/update")) {
            return apiResponse({
                id: 9001
            });
        }

        if (path.endsWith("/classics/sancai/contents/delete")) {
            return apiResponse(true);
        }

        if (path.endsWith("/classics/sancai/contents/sort")) {
            return apiResponse(true);
        }

        return apiResponse(true);
    });
};

const expectCall = (path: string, body: unknown) => {
    expect(capturedCalls).toContainEqual({
        body,
        method: "POST",
        path
    });
};

const hasCall = (path: string) => {
    return capturedCalls.some((call) => call.path === path);
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

const renderContentPanel = (entryId: number | null) => {
    return render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <SancaiContentPanel
                    entry={entryId === null ? null : { id: entryId, title: "天地" }}
                    refreshVersion={0}
                />
            </AntdApp>
        </QueryClientProvider>
    );
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

    it("wires volume sort controls to backend requests", async () => {
        const user = userEvent.setup();

        renderSancaiPage();

        expect(await screen.findByRole("heading", { name: "三才图会" })).toBeInTheDocument();
        expect(
            await screen.findByRole("button", { name: "选择卷目 天文卷一" })
        ).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "调整三才图会卷目顺序" }));
        await user.click(screen.getByRole("button", { name: "下移卷目 天文卷一" }));
        await user.click(screen.getByRole("button", { name: "保存三才图会卷目顺序" }));

        await waitFor(() =>
            expectCall("/classics/sancai/volumes/sort", {
                orderedIds: [102, 101],
                sortDirection: "ASC"
            })
        );
    }, 10000);

    it("wires volume add, update, and delete controls to backend requests", async () => {
        const user = userEvent.setup();

        renderSancaiPage();

        expect(await screen.findByRole("heading", { name: "三才图会" })).toBeInTheDocument();
        expect(
            await screen.findByRole("button", { name: "选择卷目 天文卷一" })
        ).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "新增三才图会卷目" }));
        await user.type(screen.getByLabelText("三才图会卷目标题"), "新卷");
        await user.click(screen.getByRole("button", { name: "保存新增卷目" }));

        await waitFor(() =>
            expectCall("/classics/sancai/volumes/add", {
                categoryId: 2,
                title: "新卷",
                volumeType: "MAIN"
            })
        );

        await user.click(screen.getByRole("button", { name: "编辑卷目 天文卷一" }));
        const titleInput = screen.getByLabelText("三才图会卷目标题");
        await user.clear(titleInput);
        await user.type(titleInput, "天文卷一修订");
        await user.click(screen.getByRole("button", { name: "保存卷目 天文卷一" }));

        await waitFor(() =>
            expectCall("/classics/sancai/volumes/update", {
                categoryId: 2,
                id: 101,
                title: "天文卷一修订",
                volumeType: "MAIN"
            })
        );

        await user.click(screen.getByRole("button", { name: "删除卷目 天文卷一" }));
        const deleteButtons = await screen.findAllByRole("button", { name: /删\s*除/ });
        const confirmDeleteButton = deleteButtons.find(
            (button) => button.textContent?.replace(/\s/g, "") === "删除"
        );
        expect(confirmDeleteButton).toBeDefined();
        await user.click(confirmDeleteButton!);

        await waitFor(() =>
            expectCall("/classics/sancai/volumes/delete", {
                id: 101
            })
        );
    }, 10000);

    it("requests content only after receiving an entry", async () => {
        const { rerender } = renderContentPanel(null);

        expect(hasCall("/classics/sancai/contents/list")).toBe(false);

        rerender(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SancaiContentPanel entry={{ id: 3001, title: "天地" }} refreshVersion={0} />
                </AntdApp>
            </QueryClientProvider>
        );

        await waitFor(() =>
            expectCall("/classics/sancai/contents/list", {
                entryId: 3001
            })
        );
        expect(await screen.findByText("何为天地")).toBeInTheDocument();
    }, 10000);
});
