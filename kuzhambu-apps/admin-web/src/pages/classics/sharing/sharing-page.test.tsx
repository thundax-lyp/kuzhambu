import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
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

const installSharingFetchMock = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
        const url = readFetchUrl(input);
        const path = url.replace("/kuzhambu-admin-api/api", "");
        capturedCalls.push({
            body: init?.body ? JSON.parse(String(init.body)) : undefined,
            method: init?.method,
            path
        });

        if (path.endsWith("/classics/shares/page")) {
            return apiResponse({
                pageNo: 1,
                pageSize: 20,
                totalCount: 1,
                count: 1,
                totalPage: 1,
                records: [
                    {
                        id: 900000000001,
                        title: "王圻文档 分享",
                        shareToken: "token-abc",
                        shareUrl: "/classics/share/token-abc",
                        status: "ACTIVE",
                        visibility: "PUBLIC",
                        accessCount: 12,
                        issuedAt: "2026-01-01T00:00:00.000+00:00",
                        expiresAt: "2026-12-31T00:00:00.000+00:00",
                        targets: [
                            {
                                id: 1,
                                contentType: "WANGQI_DOCUMENT",
                                contentId: 101,
                                titleSnapshot: "王圻文档#1"
                            }
                        ]
                    }
                ]
            });
        }

        if (path.endsWith("/classics/shares/900000000001")) {
            return apiResponse({
                id: 900000000001,
                title: "王圻文档 分享",
                shareToken: "token-abc",
                shareUrl: "/classics/share/token-abc",
                status: "ACTIVE",
                visibility: "PUBLIC",
                accessCount: 12,
                issuedAt: "2026-01-01T00:00:00.000+00:00",
                expiresAt: "2026-12-31T00:00:00.000+00:00",
                targets: [
                    {
                        id: 1,
                        contentType: "WANGQI_DOCUMENT",
                        contentId: 101,
                        titleSnapshot: "王圻文档#1"
                    }
                ]
            });
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
                        id: 3001,
                        accessResult: "SUCCESS",
                        accessedAt: "2026-01-02T12:00:00.000+00:00",
                        clientSnapshot: "Chrome/Edge"
                    }
                ]
            });
        }

        if (path.endsWith("/classics/shares/status/update")) {
            return apiResponse(true);
        }

        return apiResponse(true);
    });
};

const renderSharingPage = () => {
    render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <SharingPage />
            </AntdApp>
        </QueryClientProvider>
    );
};

describe("SharingPage", () => {
    beforeEach(() => {
        capturedCalls.length = 0;
        confirmDangerMock.mockClear();
        queryClient.clear();
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        installSharingFetchMock();
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        localStorage.clear();
        vi.restoreAllMocks();
    });

    it("loads sharing list and applies filters", async () => {
        const user = userEvent.setup();
        renderSharingPage();

        expect(await screen.findByRole("heading", { name: "分享管理" })).toBeInTheDocument();
        expect(await screen.findByText("王圻文档 分享")).toBeInTheDocument();
        await waitFor(() => {
            expect(capturedCalls).toContainEqual({
                method: "POST",
                path: "/classics/shares/page",
                body: {
                    pageNo: 1,
                    pageSize: 20
                }
            });
        });

        await user.click(screen.getByRole("button", { name: "filter 筛选" }));
        await user.click(screen.getByLabelText("分享内容类型"));
        await user.click(await screen.findByTitle("三才条目"));
        await user.click(screen.getByLabelText("分享状态"));
        await user.click(await screen.findByTitle("已过期"));
        await user.click(screen.getByLabelText("分享可见性"));
        await user.click(await screen.findByTitle("私有"));
        await user.click(screen.getByRole("button", { name: /查\s*询/ }));

        await waitFor(() => {
            expect(capturedCalls).toContainEqual(
                expect.objectContaining({
                    method: "POST",
                    path: "/classics/shares/page",
                    body: expect.objectContaining({
                        pageNo: 1,
                        pageSize: 20,
                        contentType: "SANCAI_ENTRY",
                        status: "EXPIRED",
                        visibility: "PRIVATE"
                    })
                })
            );
        });
    }, 15000);

    it("opens detail and updates status", async () => {
        const user = userEvent.setup();
        renderSharingPage();

        await screen.findByText("王圻文档 分享");
        const row = screen.getByText("王圻文档 分享").closest("tr");
        if (!row) {
            throw new Error("共享记录行未找到");
        }
        await waitFor(() => {
            expect(
                within(row).getByRole("button", { name: /查看 .*王圻文档 分享-王圻文档/ })
            ).toBeInTheDocument();
        });
        fireEvent.click(
            within(row).getByRole("button", {
                name: /查看 .*王圻文档 分享-王圻文档/
            })
        );

        expect(await screen.findByText("分享详情")).toBeInTheDocument();

        await waitFor(() => {
            expect(capturedCalls).toContainEqual({
                method: "GET",
                path: "/classics/shares/900000000001",
                body: undefined
            });
        });
        expect(await screen.findByText("王圻文档#1")).toBeInTheDocument();

        await user.click(screen.getByRole("combobox", { name: "更新状态" }));
        fireEvent.click(await screen.findByText("已过期"));
        expect(confirmDangerMock).toHaveBeenCalledTimes(1);
        await waitFor(() => {
            expect(capturedCalls).toContainEqual({
                method: "POST",
                path: "/classics/shares/status/update",
                body: {
                    id: 900000000001,
                    status: "EXPIRED"
                }
            });
        });
    }, 15000);
});
