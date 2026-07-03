import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ShareForm } from "./share-form";
import * as shareService from "./share-service";

vi.mock("./share-service", () => ({
    getShare: vi.fn(),
    getShareResourceContentUrl: vi.fn(() => "http://localhost/resource")
}));

const renderShareForm = (shareToken: string) => {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: {
                retry: false
            }
        }
    });

    render(
        <QueryClientProvider client={queryClient}>
            <MemoryRouter initialEntries={[`/share/${shareToken}`]}>
                <Routes>
                    <Route path="/share/:shareToken" element={<ShareForm />} />
                </Routes>
            </MemoryRouter>
        </QueryClientProvider>
    );

    return queryClient;
};

describe("ShareForm", () => {
    afterEach(() => {
        cleanup();
        vi.restoreAllMocks();
    });

    it("renders an active batch-created share with the existing response fields", async () => {
        vi.mocked(shareService.getShare).mockResolvedValue({
            issuedAt: "2026-01-01T00:00:00.000+08:00",
            status: "ACTIVE",
            targets: [
                {
                    contentId: 400000000001,
                    contentSnapshotJson: JSON.stringify({
                        summary: "记录王圻古籍条目。",
                        content: "## 王圻"
                    }),
                    contentType: "WANGQI_DOCUMENT",
                    contentVersionId: 8101,
                    contentVersionNo: 1,
                    contentVisibilitySnapshot: "PUBLIC",
                    targetStatus: "ACTIVE",
                    titleSnapshot: "王圻文档"
                }
            ],
            title: "王圻批量分享 - 王圻文档",
            visibility: "PUBLIC"
        });

        renderShareForm("active-token");

        expect(
            await screen.findByRole("heading", { name: "王圻批量分享 - 王圻文档" })
        ).toBeTruthy();
        expect(await screen.findByText("王圻文档")).toBeTruthy();
        expect(await screen.findAllByText("ACTIVE")).toHaveLength(2);
    });

    it.each(["EXPIRED", "REVOKED"])(
        "keeps %s shares on the existing error state",
        async (status) => {
            vi.mocked(shareService.getShare).mockRejectedValue(
                new Error(`share ${status.toLowerCase()}`)
            );

            renderShareForm(`${status.toLowerCase()}-token`);

            const errorState = await screen.findByLabelText("分享错误状态");
            expect(errorState.textContent).toContain("分享内容不存在或已过期");
        }
    );
});
