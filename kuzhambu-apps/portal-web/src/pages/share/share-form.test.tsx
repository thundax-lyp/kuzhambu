import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
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

    it("renders sancai share images from share resources with thumbnail switching", async () => {
        const user = userEvent.setup();
        vi.mocked(shareService.getShare).mockResolvedValue({
            status: "ACTIVE",
            targets: [
                {
                    contentId: 3001,
                    contentSnapshotJson: JSON.stringify({
                        originalText: "天地玄黄",
                        summary: "三才图会摘要"
                    }),
                    contentType: "SANCAI_ENTRY",
                    contentVersionId: 9101,
                    contentVersionNo: 3,
                    contentVisibilitySnapshot: "PUBLIC",
                    images: [
                        {
                            currentUsed: true,
                            imageId: 8001,
                            imageType: "ORIGINAL",
                            originalFilename: "sancai.png",
                            priority: 2,
                            size: 2048,
                            storageObject: {
                                downloadUrl: "/portal/resource/8001?download=true",
                                previewUrl: "/portal/resource/8001",
                                storageObjectId: 7001
                            },
                            storageObjectId: 7001,
                            title: "原图"
                        },
                        {
                            currentUsed: false,
                            imageId: 8002,
                            imageType: "GENERATED",
                            originalFilename: "generated.png",
                            priority: 1,
                            size: 1024,
                            storageObject: {
                                downloadUrl: "/portal/resource/8002?download=true",
                                previewUrl: "/portal/resource/8002",
                                storageObjectId: 7002
                            },
                            storageObjectId: 7002,
                            title: "生成图"
                        }
                    ],
                    targetStatus: "ACTIVE",
                    titleSnapshot: "天地"
                }
            ],
            title: "三才分享",
            visibility: "PUBLIC"
        });

        renderShareForm("sancai-token");

        const imageSection = await screen.findByLabelText("三才图会图片");
        expect(within(imageSection).getByRole("img", { name: "原图" }).getAttribute("src")).toBe(
            "/portal/resource/8001"
        );
        expect(
            within(imageSection).getByRole("link", { name: "下载原图" }).getAttribute("href")
        ).toBe("/portal/resource/8001?download=true");

        expect(
            within(imageSection).getByRole("link", { name: "下载原图" }).getAttribute("href")
        ).not.toContain("/api/classics/sancai/assets/images");
        expect(
            within(imageSection).getByRole("img", { name: "原图" }).getAttribute("src")
        ).not.toContain("/api/classics/sancai/assets/images");

        const thumbnails = within(imageSection).getAllByRole("button", { name: /切换图片/ });
        expect(thumbnails.map((thumbnail) => thumbnail.textContent)).toEqual([
            "生成图",
            "原图当前使用"
        ]);

        await user.click(within(imageSection).getByRole("button", { name: "切换图片 生成图" }));

        expect(within(imageSection).getByRole("img", { name: "生成图" }).getAttribute("src")).toBe(
            "/portal/resource/8002"
        );
        expect(
            within(imageSection).getByRole("link", { name: "下载原图" }).getAttribute("href")
        ).toBe("/portal/resource/8002?download=true");
        expect(shareService.getShareResourceContentUrl).not.toHaveBeenCalled();
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
