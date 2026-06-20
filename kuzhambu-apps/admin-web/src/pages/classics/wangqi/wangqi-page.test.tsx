import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { WangqiPage } from "./wangqi-page";

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

const readFetchBody = (body: BodyInit | null | undefined) => {
    if (!body) {
        return undefined;
    }
    if (body instanceof FormData) {
        return Object.fromEntries(
            Array.from(body.entries()).map(([key, value]) => [
                key,
                value instanceof File ? value.name : String(value)
            ])
        );
    }
    return JSON.parse(String(body));
};

const wangqiRecord = {
    id: 400000000001,
    title: "王圻文档",
    summary: "记录王圻古籍条目。",
    contentFormat: "MARKDOWN",
    content: "## 王圻\n\n古籍正文。",
    documentTime: "2026-01-01T00:00:00.000+00:00",
    storageObjectId: 7001,
    visibility: "PUBLIC"
};

const installWangqiFetchMock = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
        const url = readFetchUrl(input);
        const path = url.replace("/kuzhambu-admin-api/api", "");
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
                records: [wangqiRecord]
            });
        }
        if (path.endsWith("/classics/wangqi/documents/timeline/list")) {
            return apiResponse([wangqiRecord]);
        }
        if (path.endsWith("/classics/wangqi/documents/400000000001/get")) {
            return apiResponse({
                ...wangqiRecord,
                contentFormat: "HTML",
                content: "<h2>王圻</h2><script>alert(1)</script>"
            });
        }
        if (path.endsWith("/classics/wangqi/documents/400000000001/source-file/get")) {
            return apiResponse({
                documentId: 400000000001,
                storageObjectId: 7001,
                originalFilename: "wangqi.pdf",
                contentType: "application/pdf",
                size: 10,
                contentUrl: "/classics/wangqi/documents/400000000001/source-file/content"
            });
        }
        if (path.endsWith("/classics/wangqi/documents/400000000001/source-file/upload")) {
            return apiResponse({
                documentId: 400000000001,
                storageObjectId: 7002,
                originalFilename: "new-wangqi.pdf",
                contentType: "application/pdf",
                size: 12
            });
        }
        if (path.endsWith("/classics/wangqi/documents/versions/list")) {
            return apiResponse([
                {
                    id: 9001,
                    contentType: "WANGQI_DOCUMENT",
                    contentId: 400000000001,
                    versionNo: 1,
                    versionedAt: "2026-01-01T00:00:00.000+00:00",
                    snapshotJson: JSON.stringify({
                        title: "历史王圻文档",
                        summary: "历史摘要",
                        contentFormat: "MARKDOWN",
                        content: "历史正文",
                        documentTime: "2025-01-01T00:00:00.000+00:00",
                        storageObjectId: 6001,
                        visibility: "PRIVATE"
                    }),
                    changeType: "MANUAL_SAVE",
                    changeSummary: "保存王圻文档"
                }
            ]);
        }
        if (path.endsWith("/classics/wangqi/documents/versions/get")) {
            return apiResponse({
                id: 9001,
                contentType: "WANGQI_DOCUMENT",
                contentId: 400000000001,
                versionNo: 1,
                versionedAt: "2026-01-01T00:00:00.000+00:00",
                snapshotJson: JSON.stringify({
                    title: "历史王圻文档",
                    storageObjectId: 6001,
                    visibility: "PRIVATE"
                }),
                changeType: "MANUAL_SAVE",
                changeSummary: "保存王圻文档"
            });
        }
        if (path.endsWith("/classics/wangqi/documents/versions/reset")) {
            return apiResponse({
                id: 9002,
                versionNo: 2,
                changeType: "HISTORY_RESTORED"
            });
        }
        if (
            path.endsWith("/classics/wangqi/documents/add") ||
            path.endsWith("/classics/wangqi/documents/update") ||
            path.endsWith("/classics/wangqi/documents/delete")
        ) {
            return apiResponse(true);
        }
        return apiResponse(true);
    });
};

const renderWangqiPage = () => {
    render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <WangqiPage />
            </AntdApp>
        </QueryClientProvider>
    );
};

describe("WangqiPage", () => {
    beforeEach(() => {
        capturedCalls.length = 0;
        confirmDangerMock.mockClear();
        queryClient.clear();
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        installWangqiFetchMock();
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        localStorage.clear();
        vi.restoreAllMocks();
    });

    it("loads list and filters by keyword visibility and timeline query", async () => {
        const user = userEvent.setup();
        renderWangqiPage();

        expect(await screen.findByRole("heading", { name: "王圻文档" })).toBeInTheDocument();
        expect(await screen.findByText("王圻文档")).toBeInTheDocument();
        await waitFor(() => {
            expect(capturedCalls).toContainEqual({
                method: "POST",
                path: "/classics/wangqi/documents/page",
                body: {
                    pageNo: 1,
                    pageSize: 20,
                    sortDirection: "DESC"
                }
            });
        });

        await user.type(screen.getByRole("textbox", { name: "搜索王圻文档" }), "万历");
        await waitFor(() => {
            expect(capturedCalls).toContainEqual(
                expect.objectContaining({
                    method: "POST",
                    path: "/classics/wangqi/documents/page",
                    body: expect.objectContaining({ keyword: "万历" })
                })
            );
        });

        await user.click(screen.getByRole("button", { name: "filter 筛选" }));
        await user.click(screen.getByLabelText("王圻文档可见性"));
        await user.click(await screen.findByTitle("私有"));
        await user.click(screen.getByRole("button", { name: /查\s*询/ }));
        await waitFor(() => {
            expect(capturedCalls).toContainEqual(
                expect.objectContaining({
                    path: "/classics/wangqi/documents/page",
                    body: expect.objectContaining({ visibility: "PRIVATE" })
                })
            );
        });

        const timeline = await screen.findByLabelText("王圻文档时间线");
        await user.clear(within(timeline).getByLabelText("搜索王圻时间线"));
        await user.type(within(timeline).getByLabelText("搜索王圻时间线"), "王圻");
        await waitFor(() => {
            expect(capturedCalls).toContainEqual(
                expect.objectContaining({
                    path: "/classics/wangqi/documents/timeline/list",
                    body: expect.objectContaining({ keyword: "王圻" })
                })
            );
        });
    });

    it("opens editor, sanitizes preview and saves", async () => {
        renderWangqiPage();

        fireEvent.click(await screen.findByRole("button", { name: /查看或编辑 王圻文档/ }));
        expect(await screen.findByLabelText("王圻文档正文预览")).toBeInTheDocument();
        expect(screen.queryByText("alert(1)")).not.toBeInTheDocument();

        fireEvent.change(screen.getByLabelText("王圻文档标题"), {
            target: { value: "王圻文档修订" }
        });
        fireEvent.click(screen.getByRole("button", { name: "保存王圻文档" }));
        await waitFor(() => {
            expect(capturedCalls).toContainEqual(
                expect.objectContaining({
                    path: "/classics/wangqi/documents/update",
                    body: expect.objectContaining({
                        id: 400000000001,
                        title: "王圻文档修订"
                    })
                })
            );
        });
    }, 10000);
});
