import { afterEach, describe, expect, it, vi } from "vitest";
import * as http from "@/api/http";
import * as shareDetailService from "./share-detail-service";

describe("classics share service", () => {
    afterEach(() => {
        window.localStorage.clear();
        vi.restoreAllMocks();
    });

    it("getShare calls postJson with share token body", async () => {
        const postJsonSpy = vi
            .spyOn(http, "postJson")
            .mockResolvedValue({ title: "分享详情", targets: null });

        await shareDetailService.getShare("abc-123");

        expect(postJsonSpy).toHaveBeenCalledWith("/portal/classics/shares/get", {
            shareToken: "abc-123"
        });
    });

    it("getPrivateShare calls private share endpoint with access token", async () => {
        const postJsonWithAccessTokenSpy = vi
            .spyOn(http, "postJsonWithAccessToken")
            .mockResolvedValue({ title: "私有分享", visibility: "PRIVATE" });

        const response = await shareDetailService.getPrivateShare("private-token", "access-token");

        expect(response.visibility).toBe("PRIVATE");
        expect(postJsonWithAccessTokenSpy).toHaveBeenCalledWith(
            "/portal/classics/private-shares/get",
            { shareToken: "private-token" },
            "access-token"
        );
    });

    it("getAccessibleShare reads private share when public response requires login and token exists", async () => {
        window.localStorage.setItem("kuzhambu.admin.accessToken", "admin-token");
        const postJsonSpy = vi.spyOn(http, "postJson").mockResolvedValue({ loginRequired: true });
        const postJsonWithAccessTokenSpy = vi
            .spyOn(http, "postJsonWithAccessToken")
            .mockResolvedValue({ title: "私有分享", visibility: "PRIVATE" });

        const response = await shareDetailService.getAccessibleShare("private-token");

        expect(response.title).toBe("私有分享");
        expect(postJsonSpy).toHaveBeenCalledWith("/portal/classics/shares/get", {
            shareToken: "private-token"
        });
        expect(postJsonWithAccessTokenSpy).toHaveBeenCalledWith(
            "/portal/classics/private-shares/get",
            { shareToken: "private-token" },
            "admin-token"
        );
    });

    it("getAccessibleShare keeps login-required response when no token exists", async () => {
        vi.spyOn(http, "postJson").mockResolvedValue({
            loginRequired: true,
            visibility: "PRIVATE"
        });
        const postJsonWithAccessTokenSpy = vi.spyOn(http, "postJsonWithAccessToken");

        const response = await shareDetailService.getAccessibleShare("private-token");

        expect(response.loginRequired).toBe(true);
        expect(postJsonWithAccessTokenSpy).not.toHaveBeenCalled();
    });

    it("passes through active batch-created multi-target share payload without adding portal-only fields", async () => {
        vi.spyOn(http, "postJson").mockResolvedValue({
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

        const response = await shareDetailService.getShare("active-token");

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
        vi.spyOn(http, "postJson").mockResolvedValue({
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

        const response = await shareDetailService.getShare("token");
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
            vi.spyOn(http, "postJson").mockRejectedValue(error);

            await expect(
                shareDetailService.getShare(`${status.toLowerCase()}-token`)
            ).rejects.toThrow(error.message);
        }
    );

    it("getShareResourceContentUrl sets download flag only for preview/download mode", () => {
        const buildApiUrlSpy = vi.spyOn(http, "buildApiUrl");
        buildApiUrlSpy.mockReturnValue("http://example.com");

        shareDetailService.getShareResourceContentUrl({
            mode: "preview",
            shareToken: "abc-123",
            storageObjectId: 9001
        });

        expect(buildApiUrlSpy).toHaveBeenCalledWith(
            "/portal/classics/shares/abc-123/resources/9001/content",
            { download: undefined }
        );

        shareDetailService.getShareResourceContentUrl({
            mode: "download",
            shareToken: "abc-123",
            storageObjectId: 9001
        });

        expect(buildApiUrlSpy).toHaveBeenCalledWith(
            "/portal/classics/shares/abc-123/resources/9001/content",
            { download: "true" }
        );
        expect(
            shareDetailService.getShareResourceContentUrl({
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

        const url = shareDetailService.getShareResourceContentUrl({
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

        shareDetailService.getShareResourceContentUrl({
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
