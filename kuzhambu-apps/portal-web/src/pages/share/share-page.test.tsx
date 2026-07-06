import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { SharePage } from "./share-page";
import * as shareService from "./share-service";

vi.mock("./share-service", () => ({
    listShares: vi.fn()
}));

const renderSharePage = () => {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: {
                retry: false
            }
        }
    });

    render(
        <QueryClientProvider client={queryClient}>
            <MemoryRouter>
                <SharePage />
            </MemoryRouter>
        </QueryClientProvider>
    );

    return queryClient;
};

describe("SharePage", () => {
    afterEach(() => {
        cleanup();
        vi.restoreAllMocks();
    });

    it("filters deleted share targets from the public list", async () => {
        vi.mocked(shareService.listShares).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            records: [
                {
                    contentId: 100,
                    contentType: "SANCAI_ENTRY",
                    contentVersionNo: 1,
                    issuedAt: "2026-01-01T00:00:00.000+08:00",
                    priority: 1,
                    shareLinkId: 10,
                    shareTitle: "可见分享",
                    shareToken: "visible-token",
                    targetStatus: "AVAILABLE",
                    titleSnapshot: "可见标题"
                },
                {
                    contentId: 101,
                    contentType: "SANCAI_ENTRY",
                    contentVersionNo: 1,
                    issuedAt: "2026-01-01T00:00:00.000+08:00",
                    priority: 2,
                    shareLinkId: 11,
                    shareTitle: "已删除分享",
                    shareToken: "deleted-token",
                    targetStatus: "CONTENT_DELETED",
                    titleSnapshot: "已删除标题"
                }
            ],
            totalCount: 2,
            totalPage: 1
        });

        renderSharePage();

        expect(await screen.findByText("可见标题")).toBeTruthy();
        expect(await screen.findByText("共 1 条公开分享")).toBeTruthy();
        expect(screen.queryByText("已删除标题")).toBeNull();
        expect(screen.queryByText("已删除分享")).toBeNull();
    });
});
