import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { MingCustomsPage } from "./ming-customs-page";

const confirmDangerMock = vi.hoisted(() =>
    vi.fn((options: { onConfirm: () => unknown }) => options.onConfirm())
);

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({
        danger: confirmDangerMock
    })
}));

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
        const path = url.replace("/kuzhambu-admin-api/api", "");
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
                        content: "## 正旦\n\n士民相贺。",
                        originalExcerpts: "正旦朝贺。",
                        visibility: "PUBLIC"
                    }
                ]
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
                contentFormat: "HTML",
                content: "<h2>正旦</h2><img src=x onerror=alert(1)><script>alert(1)</script>",
                originalExcerpts: "正旦朝贺。",
                visibility: "PUBLIC"
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

        if (path.endsWith("/classics/ming-customs/add")) {
            return apiResponse({
                id: 500000000003,
                title: "新增习俗"
            });
        }

        if (path.endsWith("/classics/ming-customs/update")) {
            return apiResponse({
                id: 500000000001,
                title: "岁时礼仪：元旦朝贺"
            });
        }

        if (path.endsWith("/classics/ming-customs/delete")) {
            return apiResponse(true);
        }

        if (path.endsWith("/classics/shares/create")) {
            return apiResponse({
                id: 900000000001,
                shareToken: "abc123_-",
                shareUrl: "http://localhost:5174/share/abc123_-",
                title: "岁时礼仪：元旦朝贺 分享",
                visibility: "PUBLIC"
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
        confirmDangerMock.mockClear();
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

        await user.click(screen.getByRole("button", { name: /关键词云/ }));
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

    it("creates ming customs entry from the editor", async () => {
        const user = userEvent.setup();
        renderMingCustomsPage();

        await screen.findByText("岁时礼仪：元旦朝贺");
        await user.click(screen.getByRole("button", { name: "plus 新增明代习俗" }));
        await user.type(await screen.findByLabelText("明代习俗标题"), "新增灯市习俗");
        await user.click(screen.getByLabelText("明代习俗编辑分类"));
        await user.click(await screen.findByTitle("岁时节令"));
        await user.type(screen.getByLabelText("明代习俗概述"), "记录上元灯市。");
        await user.type(screen.getByLabelText("明代习俗正文"), "## 上元\n\n灯市连宵。");
        await user.click(screen.getByRole("button", { name: "保存新增明代习俗" }));

        await waitFor(() => {
            expect(capturedCalls).toContainEqual(
                expect.objectContaining({
                    method: "POST",
                    path: "/classics/ming-customs/add",
                    body: {
                        title: "新增灯市习俗",
                        category: "FESTIVAL",
                        summary: "记录上元灯市。",
                        contentFormat: "MARKDOWN",
                        content: "## 上元\n\n灯市连宵。",
                        visibility: "PUBLIC"
                    }
                })
            );
        });
    });

    it("edits ming customs entry and renders sanitized content preview", async () => {
        const user = userEvent.setup();
        renderMingCustomsPage();

        await user.click(
            await screen.findByRole("button", { name: "编辑明代习俗 岁时礼仪：元旦朝贺" })
        );
        const preview = await screen.findByLabelText("明代习俗正文预览");
        expect(await within(preview).findByRole("heading", { name: "正旦" })).toBeInTheDocument();
        expect(screen.queryByText("alert(1)")).not.toBeInTheDocument();
        expect(preview.querySelector("script")).toBeNull();

        const contentInput = screen.getByLabelText("明代习俗正文");
        await user.clear(contentInput);
        await user.type(contentInput, "更新后的正文");
        await user.click(screen.getByRole("button", { name: "保存明代习俗" }));

        await waitFor(() => {
            expect(capturedCalls).toContainEqual({
                method: "POST",
                path: "/classics/ming-customs/update",
                body: {
                    id: 500000000001,
                    title: "岁时礼仪：元旦朝贺",
                    category: "RITUAL",
                    chapter: "岁时礼仪",
                    section: "正旦",
                    summary: "记录明代正旦朝贺与家族拜礼。",
                    contentFormat: "HTML",
                    content: "更新后的正文",
                    originalExcerpts: "正旦朝贺。",
                    visibility: "PUBLIC"
                }
            });
        });
    });

    it("deletes ming customs entry after confirmation", async () => {
        renderMingCustomsPage();

        await screen.findByText("岁时礼仪：元旦朝贺");
        fireEvent.click(screen.getByLabelText("删除 岁时礼仪：元旦朝贺"));

        await waitFor(() => {
            expect(confirmDangerMock).toHaveBeenCalled();
            expect(capturedCalls).toContainEqual({
                method: "POST",
                path: "/classics/ming-customs/delete",
                body: {
                    id: 500000000001
                }
            });
        });
    });

    it("creates public share for ming customs entry", async () => {
        renderMingCustomsPage();

        await screen.findByText("岁时礼仪：元旦朝贺");
        fireEvent.click(screen.getByLabelText("分享 岁时礼仪：元旦朝贺"));

        await waitFor(() => {
            expect(capturedCalls).toContainEqual({
                method: "POST",
                path: "/classics/shares/create",
                body: {
                    targets: [
                        {
                            contentId: 500000000001,
                            contentType: "MING_CUSTOMS"
                        }
                    ],
                    title: "岁时礼仪：元旦朝贺 分享",
                    visibility: "PUBLIC"
                }
            });
        });
    });
});
