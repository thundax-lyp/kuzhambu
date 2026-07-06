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
});
