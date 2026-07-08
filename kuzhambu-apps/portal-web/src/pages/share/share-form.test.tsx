import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ShareForm } from "./share-form";
import * as shareService from "./share-service";

vi.mock("./share-service", () => ({
    getAccessibleShare: vi.fn(),
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
        window.localStorage.clear();
        vi.clearAllMocks();
        vi.restoreAllMocks();
    });

    it("renders an active batch-created share with the existing response fields", async () => {
        vi.mocked(shareService.getAccessibleShare).mockResolvedValue({
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
        expect(await screen.findByRole("heading", { name: "王圻文档" })).toBeTruthy();
        expect(await screen.findByText("ACTIVE")).toBeTruthy();
        expect(await screen.findByText("可用")).toBeTruthy();
    });

    it("renders one share link as multiple readonly content cards with deleted placeholder", async () => {
        vi.mocked(shareService.getShareResourceContentUrl).mockReturnValue(
            "http://localhost/wangqi-resource"
        );
        vi.mocked(shareService.getAccessibleShare).mockResolvedValue({
            issuedAt: "2026-01-01T00:00:00.000+08:00",
            status: "ACTIVE",
            targets: [
                {
                    contentId: 400000000001,
                    contentSnapshotJson: JSON.stringify({
                        content: "王圻正文",
                        summary: "王圻摘要"
                    }),
                    contentType: "WANGQI_DOCUMENT",
                    contentVersionId: 8101,
                    contentVersionNo: 1,
                    contentVisibilitySnapshot: "PUBLIC",
                    storageObject: {
                        originalFilename: "wangqi.pdf",
                        storageObjectId: 7001
                    },
                    targetStatus: "ACTIVE",
                    titleSnapshot: "王圻文档"
                },
                {
                    contentId: 300000000001,
                    contentSnapshotJson: JSON.stringify({
                        originalText: "天地玄黄",
                        summary: "三才摘要"
                    }),
                    contentType: "SANCAI_ENTRY",
                    contentVersionId: 9101,
                    contentVersionNo: 2,
                    contentVisibilitySnapshot: "PUBLIC",
                    targetStatus: "ACTIVE",
                    titleSnapshot: "三才条目"
                },
                {
                    contentId: 500000000001,
                    contentSnapshotJson: JSON.stringify({ summary: "不应展示" }),
                    contentType: "MING_CUSTOMS",
                    contentVersionId: 9201,
                    contentVersionNo: 4,
                    contentVisibilitySnapshot: "PUBLIC",
                    targetStatus: "CONTENT_DELETED",
                    titleSnapshot: "已删除民俗"
                }
            ],
            title: "多内容分享",
            visibility: "PUBLIC"
        });

        renderShareForm("multi-token");

        const targets = await screen.findByLabelText("分享快照");
        expect(within(targets).getByLabelText("王圻文档内容卡片")).toBeTruthy();
        expect(within(targets).getByLabelText("三才条目内容卡片")).toBeTruthy();
        expect(within(targets).getByLabelText("已删除民俗内容卡片")).toBeTruthy();
        expect(within(targets).getByText("王圻摘要")).toBeTruthy();
        expect(within(targets).getByText("三才摘要")).toBeTruthy();
        expect(within(targets).getByText("内容已删除，分享仅保留标题快照。")).toBeTruthy();
        expect(within(targets).queryByText("不应展示")).toBeNull();
        expect(shareService.getShareResourceContentUrl).toHaveBeenCalledWith({
            mode: "preview",
            privateAccess: false,
            shareToken: "multi-token",
            storageObjectId: 7001
        });
        expect(shareService.getShareResourceContentUrl).toHaveBeenCalledWith({
            mode: "download",
            privateAccess: false,
            shareToken: "multi-token",
            storageObjectId: 7001
        });
    });

    it("renders sancai share images from share resources with thumbnail switching", async () => {
        const user = userEvent.setup();
        vi.mocked(shareService.getAccessibleShare).mockResolvedValue({
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
            vi.mocked(shareService.getAccessibleShare).mockRejectedValue(
                new Error(`share ${status.toLowerCase()}`)
            );

            renderShareForm(`${status.toLowerCase()}-token`);

            const errorState = await screen.findByLabelText("分享错误状态");
            expect(errorState.textContent).toContain("分享内容不存在或已过期");
        }
    );

    it("shows login guidance for private share without local login", async () => {
        vi.mocked(shareService.getAccessibleShare).mockResolvedValue({
            loginRequired: true,
            targets: [],
            visibility: "PRIVATE"
        });

        renderShareForm("private-token");

        const loginGuide = await screen.findByLabelText("私有分享登录引导");
        expect(loginGuide.textContent).toContain("私有分享需要登录后访问");
        expect(screen.queryByLabelText("分享快照")).toBeNull();
    });

    it("uses private resource urls for authenticated private shares", async () => {
        vi.mocked(shareService.getShareResourceContentUrl).mockReturnValue(
            "http://localhost/private-resource"
        );
        vi.mocked(shareService.getAccessibleShare).mockResolvedValue({
            status: "ACTIVE",
            targets: [
                {
                    contentId: 3001,
                    contentSnapshotJson: JSON.stringify({ summary: "私有摘要" }),
                    contentType: "SANCAI_ENTRY",
                    images: [
                        {
                            currentUsed: true,
                            imageId: 8001,
                            priority: 1,
                            storageObject: {
                                previewUrl: "/public/preview",
                                storageObjectId: 7001
                            },
                            storageObjectId: 7001,
                            title: "私有图"
                        }
                    ],
                    targetStatus: "ACTIVE",
                    titleSnapshot: "私有条目"
                }
            ],
            title: "私有分享",
            visibility: "PRIVATE"
        });

        renderShareForm("private-token");

        const imageSection = await screen.findByLabelText("三才图会图片");
        expect(within(imageSection).getByRole("img", { name: "私有图" }).getAttribute("src")).toBe(
            "http://localhost/private-resource"
        );
        expect(shareService.getShareResourceContentUrl).toHaveBeenCalledWith({
            mode: "preview",
            privateAccess: true,
            shareToken: "private-token",
            storageObjectId: 7001
        });
    });

    it("renders deleted target as a placeholder without body or resource controls", async () => {
        vi.mocked(shareService.getShareResourceContentUrl).mockClear();
        vi.mocked(shareService.getAccessibleShare).mockResolvedValue({
            status: "ACTIVE",
            targets: [
                {
                    contentId: 400000000099,
                    contentSnapshotJson: JSON.stringify({
                        content: "王圻正文",
                        summary: "王圻摘要"
                    }),
                    contentType: "WANGQI_DOCUMENT",
                    contentVersionId: 8109,
                    contentVersionNo: 4,
                    contentVisibilitySnapshot: "PUBLIC",
                    storageObject: {
                        downloadUrl: "/portal/resource/7010?download=true",
                        originalFilename: "deleted.pdf",
                        previewUrl: "/portal/resource/7010",
                        storageObjectId: 7010
                    },
                    targetStatus: "CONTENT_DELETED",
                    titleSnapshot: "已删除王圻文档"
                }
            ],
            title: "删除目标分享",
            visibility: "PUBLIC"
        });

        renderShareForm("deleted-target-token");

        expect(await screen.findByText("已删除王圻文档")).toBeTruthy();
        expect(await screen.findAllByText("内容已删除")).toHaveLength(2);
        expect(await screen.findByText("内容已删除，分享仅保留标题快照。")).toBeTruthy();
        expect(screen.queryByText("王圻正文")).toBeNull();
        expect(screen.queryByText("王圻摘要")).toBeNull();
        expect(screen.queryByLabelText("王圻原始文件")).toBeNull();
        expect(screen.queryByRole("link", { name: "预览" })).toBeNull();
        expect(screen.queryByRole("link", { name: "下载" })).toBeNull();
        expect(shareService.getShareResourceContentUrl).not.toHaveBeenCalled();
    });
});
