import { afterEach, describe, expect, it, vi } from "vitest";
import * as http from "@/api/http";
import * as shareService from "./share-service";

describe("classics share service", () => {
    afterEach(() => {
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
});
