import { beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./search-statistic-service";

const postJson = vi.hoisted(() => vi.fn());

vi.mock("@/api/http", () => ({
    postJson
}));

describe("search statistic service contracts", () => {
    beforeEach(() => {
        postJson.mockReset();
    });

    it("maps search admin endpoints and request bodies", async () => {
        await service.pageSearchEvents({
            dateFrom: "2026-01-01T00:00:00.000Z",
            dateTo: "2026-01-02T23:59:59.000Z",
            intentTypes: ["REWRITE"],
            operatorId: "admin",
            pageNo: 1,
            pageSize: 20,
            queryText: "礼器",
            searchStatuses: ["SUCCESS"]
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search-statistics/events/page", {
            body: {
                dateFrom: "2026-01-01T00:00:00.000Z",
                dateTo: "2026-01-02T23:59:59.000Z",
                intentTypes: ["REWRITE"],
                operatorId: "admin",
                pageNo: 1,
                pageSize: 20,
                queryText: "礼器",
                searchStatuses: ["SUCCESS"]
            }
        });

        await service.getSearchEventDetail({ id: "EVT-1001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search-statistics/events/get", {
            body: {
                id: "EVT-1001"
            }
        });

        await service.rebuildSearchIndex({ confirm: true });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search-statistics/index/rebuild", {
            body: {
                confirm: true
            }
        });
    });
});
