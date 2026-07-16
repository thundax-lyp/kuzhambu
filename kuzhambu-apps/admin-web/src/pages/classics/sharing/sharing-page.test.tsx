import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { SharingPage } from "./sharing-page";

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
    vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
        const path = readFetchUrl(input).replace("/kuzhambu-admin-api/api", "");
        capturedCalls.push({
            body: init?.body ? JSON.parse(String(init.body)) : undefined,
            method: init?.method || "GET",
            path
        });
        if (path.endsWith("/classics/shares/page")) {
            return apiResponse({
                pageNo: 1,
                pageSize: 20,
                totalCount: 3,
                count: 3,
                totalPage: 1,
                records: [
                    {
                        id: 900000000001,
                        title: "王圻批量分享 - 王圻文档",
                        shareToken: "token-abc",
                        shareUrl: "/classics/share/token-abc",
                        status: "ACTIVE",
                        visibility: "PUBLIC",
                        accessCount: 12,
                        issuedAt: "2026-01-01T00:00:00.000+00:00",
                        expiresAt: "2026-12-31T00:00:00.000+00:00",
                        targets: [
                            {
                                id: 800000000001,
                                contentId: 400000000001,
                                contentType: "WANGQI_DOCUMENT",
                                targetStatus: "AVAILABLE",
                                titleSnapshot: "王圻文档"
                            }
                        ]
                    },
                    {
                        id: 900000000002,
                        title: "三才批量分享 - 天地",
                        shareToken: "token-expired",
                        shareUrl: "/classics/share/token-expired",
                        status: "EXPIRED",
                        visibility: "PUBLIC",
                        accessCount: 2,
                        issuedAt: "2026-01-01T00:00:00.000+00:00",
                        targets: [
                            {
                                id: 800000000002,
                                contentId: 3001,
                                contentType: "SANCAI_ENTRY",
                                targetStatus: "AVAILABLE",
                                titleSnapshot: "天地"
                            }
                        ]
                    },
                    {
                        id: 900000000003,
                        title: "明代习俗批量分享 - 元旦朝贺",
                        shareToken: "token-revoked",
                        shareUrl: "/classics/share/token-revoked",
                        status: "REVOKED",
                        visibility: "PUBLIC",
                        accessCount: 0,
                        issuedAt: "2026-01-01T00:00:00.000+00:00",
                        targets: [
                            {
                                id: 800000000003,
                                contentId: 500000000001,
                                contentType: "MING_CUSTOMS",
                                targetStatus: "AVAILABLE",
                                titleSnapshot: "岁时礼仪：元旦朝贺"
                            }
                        ]
                    }
                ]
            });
        }
        if (path.endsWith("/classics/shares/status/update")) {
            return apiResponse(true);
        }
        if (path.endsWith("/classics/shares/access-records/page")) {
            return apiResponse({
                pageNo: 1,
                pageSize: 20,
                totalCount: 2,
                count: 2,
                totalPage: 1,
                records: [
                    {
                        id: 700000000001,
                        shareLinkId: 900000000001,
                        shareTargetId: null,
                        accessedAt: "2026-01-02T00:00:00.000+00:00",
                        accessResult: "ALLOWED",
                        clientSnapshot: JSON.stringify({
                            accessType: "DETAIL_VIEW",
                            privateAccess: false
                        })
                    },
                    {
                        id: 700000000002,
                        shareLinkId: 900000000001,
                        shareTargetId: 800000000001,
                        accessedAt: "2026-01-02T01:00:00.000+00:00",
                        accessResult: "ALLOWED",
                        clientSnapshot: JSON.stringify({
                            accessType: "RESOURCE_READ",
                            privateAccess: false,
                            storageObjectId: 600000000001,
                            download: true
                        })
                    }
                ]
            });
        }
        if (path.endsWith("/classics/shares/get")) {
            return apiResponse({
                id: 900000000001,
                title: "王圻批量分享 - 王圻文档",
                shareToken: "token-abc",
                shareUrl: "/classics/share/token-abc",
                status: "ACTIVE",
                visibility: "PUBLIC",
                accessCount: 12,
                issuedAt: "2026-01-01T00:00:00.000+00:00",
                expiresAt: "2026-12-31T00:00:00.000+00:00",
                targets: [
                    {
                        id: 800000000001,
                        contentId: 400000000001,
                        contentType: "WANGQI_DOCUMENT",
                        targetStatus: "AVAILABLE",
                        titleSnapshot: "王圻文档"
                    },
                    {
                        id: 800000000004,
                        contentId: 400000000099,
                        contentType: "WANGQI_DOCUMENT",
                        targetStatus: "CONTENT_DELETED",
                        titleSnapshot: "已删除王圻文档"
                    }
                ]
            });
        }
        return apiResponse(true);
    });
};

describe("SharingPage", () => {
    beforeEach(() => {
        capturedCalls.length = 0;
        confirmDangerMock.mockClear();
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

    it("renders page and sharing record", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SharingPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "分享管理" })).toBeInTheDocument();
        expect(await screen.findByText("王圻批量分享 - 王圻文档")).toBeInTheDocument();
    }, 30000);

    it("keeps batch-created shares manageable by revoke and restore actions", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SharingPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByText("王圻批量分享 - 王圻文档")).toBeInTheDocument();
        expect(await screen.findByText("三才批量分享 - 天地")).toBeInTheDocument();
        expect(await screen.findByText("明代习俗批量分享 - 元旦朝贺")).toBeInTheDocument();

        await user.click(
            screen.getByRole("button", {
                name: "撤销 王圻批量分享 - 王圻文档-王圻文档"
            })
        );
        await user.click(
            screen.getByRole("button", {
                name: "恢复 明代习俗批量分享 - 元旦朝贺-明人志异"
            })
        );
        expect(
            screen.queryByRole("button", {
                name: "恢复 三才批量分享 - 天地-三才条目"
            })
        ).not.toBeInTheDocument();

        await waitFor(() => {
            const statusCalls = capturedCalls.filter((call) =>
                call.path.endsWith("/classics/shares/status/update")
            );
            expect(statusCalls).toHaveLength(2);
            expect(statusCalls.map((call) => call.body)).toEqual([
                {
                    id: 900000000001,
                    status: "REVOKED"
                },
                {
                    id: 900000000003,
                    status: "ACTIVE"
                }
            ]);
        });
    }, 30000);

    it("shows deleted target placeholder in sharing detail drawer", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SharingPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByText("王圻批量分享 - 王圻文档")).toBeInTheDocument();

        await user.click(
            screen.getByRole("button", {
                name: "查看 王圻批量分享 - 王圻文档-王圻文档"
            })
        );

        expect(await screen.findByText("关联内容")).toBeInTheDocument();
        expect(await screen.findByText("已删除王圻文档")).toBeInTheDocument();
        expect(await screen.findByText("内容已删除")).toBeInTheDocument();
        expect(await screen.findByText("详情浏览")).toBeInTheDocument();
        expect(await screen.findByText("资源读取")).toBeInTheDocument();
        expect(await screen.findByText("800000000001")).toBeInTheDocument();
        expect(
            screen.queryByRole("button", { name: /打开已删除王圻文档/ })
        ).not.toBeInTheDocument();
        expect(
            screen.queryByRole("button", { name: /下载已删除王圻文档/ })
        ).not.toBeInTheDocument();
        expect(
            screen.queryByRole("button", { name: /预览已删除王圻文档/ })
        ).not.toBeInTheDocument();
    }, 30000);
});
