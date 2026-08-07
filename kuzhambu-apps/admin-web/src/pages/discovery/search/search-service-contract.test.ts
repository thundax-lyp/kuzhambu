import { beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./search-service";

const postJson = vi.hoisted(() => vi.fn());

vi.mock("@/api/http", () => ({
    postJson
}));

describe("search service contracts", () => {
    beforeEach(() => {
        postJson.mockReset();
    });

    it("maps search consumer endpoints and request bodies", async () => {
        await service.searchDiscovery({
            categoryCodes: ["SANCAI_ENTRY"],
            dateFrom: "2026-01-01T00:00:00.000Z",
            dateTo: "2026-01-02T23:59:59.000Z",
            knowledgeBases: ["SANCAI_ENTRY"],
            pageNo: 1,
            pageSize: 10,
            queryText: "辞官",
            tagNames: ["礼制"]
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search/search", {
            body: {
                categoryCodes: ["SANCAI_ENTRY"],
                dateFrom: "2026-01-01T00:00:00.000Z",
                dateTo: "2026-01-02T23:59:59.000Z",
                knowledgeBases: ["SANCAI_ENTRY"],
                pageNo: 1,
                pageSize: 10,
                queryText: "辞官",
                tagNames: ["礼制"]
            }
        });

        await service.clickSearchResult({
            contentDomain: "classics",
            contentId: "1001",
            contentTitle: "礼器",
            contentType: "SANCAI_ENTRY",
            groupRank: 1,
            resultGroupKey: "SANCAI_ENTRY",
            resultRank: 1,
            searchEventId: "EVT-1001",
            targetPath: "/classics/sancai"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search/click", {
            body: {
                contentDomain: "classics",
                contentId: "1001",
                contentTitle: "礼器",
                contentType: "SANCAI_ENTRY",
                groupRank: 1,
                resultGroupKey: "SANCAI_ENTRY",
                resultRank: 1,
                searchEventId: "EVT-1001",
                targetPath: "/classics/sancai"
            }
        });

        await service.previewSearchResult({
            contentId: "1001",
            contentType: "SANCAI_ENTRY"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search/preview", {
            body: {
                contentId: "1001",
                contentType: "SANCAI_ENTRY"
            }
        });
    });
});
