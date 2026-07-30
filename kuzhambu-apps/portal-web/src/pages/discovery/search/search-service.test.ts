import { afterEach, describe, expect, it, vi } from "vitest";
import * as discoverySearchService from "./search-service";
import type { DiscoverySearchRequest } from "./search-types";

const mocks = vi.hoisted(() => ({
    postJson: vi.fn()
}));

vi.mock("@/api/http", () => mocks);

describe("discovery search service", () => {
    afterEach(() => {
        mocks.postJson.mockReset();
    });

    it("posts only the DiscoverySearchRequest contract fields", async () => {
        mocks.postJson.mockResolvedValueOnce({
            groupCount: 0,
            groups: [],
            totalCount: 0
        });

        const request = {
            categoryCodes: ["RITUAL"],
            contentStatuses: ["PUBLISHED"],
            dateFrom: "2026-01-02T00:00:00.000Z",
            dateTo: "2026-01-31T15:59:59.000Z",
            internalTrimmedCount: 10,
            knowledgeBases: ["SANCAI_ENTRY"],
            pageNo: 2,
            pageSize: 20,
            permissionDebugTrace: "internal-only",
            queryText: "礼俗",
            tagNames: ["礼制"],
            visibilityScopes: ["PUBLIC"]
        } as DiscoverySearchRequest & {
            internalTrimmedCount: number;
            permissionDebugTrace: string;
        };

        await discoverySearchService.searchDiscovery(request);

        expect(mocks.postJson).toHaveBeenCalledWith("/portal/discovery/search/search", {
            categoryCodes: ["RITUAL"],
            contentStatuses: ["PUBLISHED"],
            dateFrom: "2026-01-02T00:00:00.000Z",
            dateTo: "2026-01-31T15:59:59.000Z",
            knowledgeBases: ["SANCAI_ENTRY"],
            pageNo: 2,
            pageSize: 20,
            queryText: "礼俗",
            tagNames: ["礼制"],
            visibilityScopes: ["PUBLIC"]
        });
    });

    it("posts search preview identity in request body", async () => {
        mocks.postJson.mockResolvedValueOnce({
            bodyText: "正文",
            contentId: "1001",
            contentType: "SANCAI_ENTRY",
            title: "礼器"
        });

        await discoverySearchService.previewSearchResult({
            contentId: "1001",
            contentType: "SANCAI_ENTRY"
        });

        expect(mocks.postJson).toHaveBeenCalledWith("/portal/discovery/search/preview", {
            contentId: "1001",
            contentType: "SANCAI_ENTRY"
        });
    });

    it("posts numeric search event id when recording result clicks", async () => {
        mocks.postJson.mockResolvedValueOnce(true);

        await discoverySearchService.recordSearchClickEvent({
            contentDomain: "CLASSICS",
            contentId: "1001",
            contentTitle: "礼器",
            contentType: "SANCAI_ENTRY",
            groupRank: 1,
            resultGroupKey: "SANCAI_ENTRY",
            resultRank: 1,
            searchEventId: "1001",
            targetPath: "/shares/1001"
        });

        expect(mocks.postJson).toHaveBeenCalledWith("/portal/discovery/search/click", {
            contentDomain: "CLASSICS",
            contentId: "1001",
            contentTitle: "礼器",
            contentType: "SANCAI_ENTRY",
            groupRank: 1,
            resultGroupKey: "SANCAI_ENTRY",
            resultRank: 1,
            searchEventId: "1001",
            targetPath: "/shares/1001"
        });
    });
});
