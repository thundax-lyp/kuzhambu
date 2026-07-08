import { afterEach, describe, expect, it, vi } from "vitest";
import * as http from "@/api/http";
import * as shareService from "./share-service";

describe("classics share service", () => {
    afterEach(() => {
        window.localStorage.clear();
        vi.restoreAllMocks();
    });

    it("listShares forwards query parameters to getJson", async () => {
        const getJsonSpy = vi.spyOn(http, "getJson").mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            records: [],
            totalCount: 0,
            totalPage: 0
        });

        const query = { title: "test", pageNo: 1 };
        await shareService.listShares(query);

        expect(getJsonSpy).toHaveBeenCalledWith("/portal/classics/shares", query);
    });

    it("getShare calls getJson with share token path", async () => {
        const getJsonSpy = vi
            .spyOn(http, "getJson")
            .mockResolvedValue({ title: "分享详情", targets: null });

        await shareService.getShare("abc-123");

        expect(getJsonSpy).toHaveBeenCalledWith("/portal/classics/shares/abc-123");
    });

    it("getPrivateShare calls private share endpoint with access token", async () => {
        const getJsonWithAccessTokenSpy = vi
            .spyOn(http, "getJsonWithAccessToken")
            .mockResolvedValue({ title: "私有分享", visibility: "PRIVATE" });

        const response = await shareService.getPrivateShare("private-token", "access-token");

        expect(response.visibility).toBe("PRIVATE");
        expect(getJsonWithAccessTokenSpy).toHaveBeenCalledWith(
            "/portal/classics/private-shares/private-token",
            "access-token"
        );
    });

    it("getAccessibleShare reads private share when public response requires login and token exists", async () => {
        window.localStorage.setItem("kuzhambu.admin.accessToken", "admin-token");
        const getJsonSpy = vi.spyOn(http, "getJson").mockResolvedValue({ loginRequired: true });
        const getJsonWithAccessTokenSpy = vi
            .spyOn(http, "getJsonWithAccessToken")
            .mockResolvedValue({ title: "私有分享", visibility: "PRIVATE" });

        const response = await shareService.getAccessibleShare("private-token");

        expect(response.title).toBe("私有分享");
        expect(getJsonSpy).toHaveBeenCalledWith("/portal/classics/shares/private-token");
        expect(getJsonWithAccessTokenSpy).toHaveBeenCalledWith(
            "/portal/classics/private-shares/private-token",
            "admin-token"
        );
    });

    it("getAccessibleShare keeps login-required response when no token exists", async () => {
        vi.spyOn(http, "getJson").mockResolvedValue({ loginRequired: true, visibility: "PRIVATE" });
        const getJsonWithAccessTokenSpy = vi.spyOn(http, "getJsonWithAccessToken");

        const response = await shareService.getAccessibleShare("private-token");

        expect(response.loginRequired).toBe(true);
        expect(getJsonWithAccessTokenSpy).not.toHaveBeenCalled();
    });

    it("passes through active batch-created multi-target share payload without adding portal-only fields", async () => {
        vi.spyOn(http, "getJson").mockResolvedValue({
            status: "ACTIVE",
            targets: [
                {
                    contentId: 400000000001,
                    contentSnapshotJson: '{"title":"王圻文档"}',
                    contentType: "WANGQI_DOCUMENT",
                    contentVersionId: 8101,
                    contentVersionNo: 1,
                    contentVisibilitySnapshot: "PUBLIC",
                    targetStatus: "ACTIVE",
                    titleSnapshot: "王圻文档"
                },
                {
                    contentId: 300000000001,
                    contentSnapshotJson: JSON.stringify({ title: "天地", images: [] }),
                    contentType: "SANCAI_ENTRY",
                    contentVersionId: 8102,
                    contentVersionNo: 3,
                    contentVisibilitySnapshot: "PUBLIC",
                    images: [],
                    priority: 2,
                    targetStatus: "ACTIVE",
                    titleSnapshot: "天地"
                }
            ],
            title: "王圻批量分享 - 王圻文档",
            visibility: "PUBLIC"
        });

        const response = await shareService.getShare("active-token");

        expect(response.targets).toHaveLength(2);
        expect(response).toEqual({
            status: "ACTIVE",
            targets: [
                {
                    contentId: 400000000001,
                    contentSnapshotJson: '{"title":"王圻文档"}',
                    contentType: "WANGQI_DOCUMENT",
                    contentVersionId: 8101,
                    contentVersionNo: 1,
                    contentVisibilitySnapshot: "PUBLIC",
                    targetStatus: "ACTIVE",
                    titleSnapshot: "王圻文档"
                },
                {
                    contentId: 300000000001,
                    contentSnapshotJson: JSON.stringify({ title: "天地", images: [] }),
                    contentType: "SANCAI_ENTRY",
                    contentVersionId: 8102,
                    contentVersionNo: 3,
                    contentVisibilitySnapshot: "PUBLIC",
                    images: [],
                    priority: 2,
                    targetStatus: "ACTIVE",
                    titleSnapshot: "天地"
                }
            ],
            title: "王圻批量分享 - 王圻文档",
            visibility: "PUBLIC"
        });
    });

    it("keeps sancai image resource urls and non-current images in share payload", async () => {
        vi.spyOn(http, "getJson").mockResolvedValue({
            status: "ACTIVE",
            targets: [
                {
                    contentId: 3001,
                    contentSnapshotJson: '{"title":"天地"}',
                    contentType: "SANCAI_ENTRY",
                    images: [
                        {
                            currentUsed: true,
                            imageId: 8001,
                            priority: 2,
                            storageObject: {
                                downloadUrl:
                                    "/portal/classics/shares/token/resources/7001/content?download=true",
                                previewUrl: "/portal/classics/shares/token/resources/7001/content",
                                storageObjectId: 7001
                            },
                            storageObjectId: 7001,
                            title: "原图"
                        },
                        {
                            currentUsed: false,
                            imageId: 8002,
                            priority: 1,
                            storageObject: {
                                downloadUrl:
                                    "/portal/classics/shares/token/resources/7002/content?download=true",
                                previewUrl: "/portal/classics/shares/token/resources/7002/content",
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

        const response = await shareService.getShare("token");
        const images = response.targets?.[0]?.images ?? [];

        expect(images).toHaveLength(2);
        expect(images.map((image) => image.currentUsed)).toEqual([true, false]);
        expect(images.map((image) => image.priority)).toEqual([2, 1]);
        expect(images[0]?.storageObject?.previewUrl).toBe(
            "/portal/classics/shares/token/resources/7001/content"
        );
        expect(images[1]?.storageObject?.downloadUrl).toBe(
            "/portal/classics/shares/token/resources/7002/content?download=true"
        );
        expect(JSON.stringify(images)).not.toContain("/api/classics/sancai/assets/images");
    });

    it.each(["EXPIRED", "REVOKED"])(
        "keeps %s share rejection in the existing getShare error path",
        async (status) => {
            const error = new Error(`share ${status.toLowerCase()}`);
            vi.spyOn(http, "getJson").mockRejectedValue(error);

            await expect(shareService.getShare(`${status.toLowerCase()}-token`)).rejects.toThrow(
                error.message
            );
        }
    );

    it("getShareResourceContentUrl sets download flag only for preview/download mode", () => {
        const buildApiUrlSpy = vi.spyOn(http, "buildApiUrl");
        buildApiUrlSpy.mockReturnValue("http://example.com");

        shareService.getShareResourceContentUrl({
            mode: "preview",
            shareToken: "abc-123",
            storageObjectId: 9001
        });

        expect(buildApiUrlSpy).toHaveBeenCalledWith(
            "/portal/classics/shares/abc-123/resources/9001/content",
            { download: undefined }
        );

        shareService.getShareResourceContentUrl({
            mode: "download",
            shareToken: "abc-123",
            storageObjectId: 9001
        });

        expect(buildApiUrlSpy).toHaveBeenCalledWith(
            "/portal/classics/shares/abc-123/resources/9001/content",
            { download: "true" }
        );
        expect(
            shareService.getShareResourceContentUrl({
                mode: "download",
                shareToken: "abc-123",
                storageObjectId: 9001
            })
        ).toBe("http://example.com");
    });

    it("getShareResourceContentUrl uses private path and token query for private resources", () => {
        window.localStorage.setItem("kuzhambu.admin.accessToken", "admin-token");
        const buildApiUrlSpy = vi.spyOn(http, "buildApiUrl");
        buildApiUrlSpy.mockReturnValue("http://example.com/private");

        const url = shareService.getShareResourceContentUrl({
            mode: "preview",
            privateAccess: true,
            shareToken: "abc-123",
            storageObjectId: 9001
        });

        expect(url).toBe("http://example.com/private");
        expect(buildApiUrlSpy).toHaveBeenCalledWith(
            "/portal/classics/private-shares/abc-123/resources/9001/content",
            { download: undefined, token: "admin-token" }
        );
    });

    it("getShareResourceContentUrl keeps private resource endpoint without token when unauthenticated", () => {
        const buildApiUrlSpy = vi.spyOn(http, "buildApiUrl");
        buildApiUrlSpy.mockReturnValue("http://example.com/private-login");

        shareService.getShareResourceContentUrl({
            mode: "download",
            privateAccess: true,
            shareToken: "private-token",
            storageObjectId: 9002
        });

        expect(buildApiUrlSpy).toHaveBeenCalledWith(
            "/portal/classics/private-shares/private-token/resources/9002/content",
            { download: "true", token: undefined }
        );
    });
});
