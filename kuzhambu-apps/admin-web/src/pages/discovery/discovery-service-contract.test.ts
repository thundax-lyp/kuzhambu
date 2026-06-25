import { beforeEach, describe, expect, it, vi } from "vitest";
import * as qaService from "./qa-admin/qa-admin-service";
import * as searchService from "./search-admin/search-admin-service";

const postJson = vi.hoisted(() => vi.fn());

vi.mock("@/api/http", () => ({
    postJson
}));

describe("discovery admin service contracts", () => {
    beforeEach(() => {
        postJson.mockReset();
    });

    it("maps qa admin endpoints and request bodies", async () => {
        await qaService.getQaSessionDetail({ sessionId: 2001 });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/session/get", {
            body: {
                sessionId: 2001
            }
        });

        await qaService.listQaSources({ messageId: 4001 });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/source/list", {
            body: {
                messageId: 4001
            }
        });

        await qaService.getQaTrace({ traceId: 9001 });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/trace/get", {
            body: {
                traceId: 9001
            }
        });
    });

    it("maps search admin endpoints and request bodies", async () => {
        await searchService.pageSearchLogs({
            dateFrom: "2026-01-01T00:00:00.000Z",
            dateTo: "2026-01-02T23:59:59.000Z",
            intentTypes: ["REWRITE"],
            operatorId: "admin",
            pageNo: 1,
            pageSize: 20,
            queryText: "礼器",
            searchStatuses: ["SUCCESS"]
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search-admin/logs/page", {
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

        await searchService.getSearchLogDetail({ searchLogId: "LOG-1001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search-admin/logs/get", {
            body: {
                searchLogId: "LOG-1001"
            }
        });

        await searchService.rebuildSearchIndex({ confirm: true });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/search-admin/index/rebuild", {
            body: {
                confirm: true
            }
        });
    });
});
