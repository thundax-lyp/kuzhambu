import { beforeEach, describe, expect, it, vi } from "vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";
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
        await qaService.getKnowledgeHealth();
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/knowledge/health");

        await qaService.rebuildKnowledge({ requestId: "REQ-1", traceId: "TRACE-1" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/knowledge/rebuild", {
            body: {
                requestId: "REQ-1",
                traceId: "TRACE-1"
            }
        });

        await qaService.createKnowledgeSync({
            contentId: 1001,
            contentType: "SANCAI_ENTRY",
            currentVersionNo: 2,
            requestId: "REQ-2",
            traceId: "TRACE-2"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/knowledge/sync", {
            body: {
                contentId: 1001,
                contentType: "SANCAI_ENTRY",
                currentVersionNo: 2,
                requestId: "REQ-2",
                traceId: "TRACE-2"
            }
        });

        await qaService.pageKnowledgeSyncItems({
            contentType: "SANCAI_ENTRY",
            pageNo: 1,
            pageSize: 10,
            syncStatus: "SYNCED"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/knowledge/sync/page", {
            body: {
                contentType: "SANCAI_ENTRY",
                pageNo: 1,
                pageSize: 10,
                syncStatus: "SYNCED"
            }
        });

        await qaService.getQaSession({ sessionId: "2001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/session/get", {
            body: {
                sessionId: "2001"
            }
        });

        await qaService.deleteQaSession({ requesterUserId: 1001, sessionId: "2001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/session/delete", {
            body: {
                requesterUserId: 1001,
                sessionId: "2001"
            }
        });

        await qaService.createQaSessionExport({
            format: "CSV",
            requesterUserId: 1001,
            sessionId: "2001"
        });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/session/export", {
            body: {
                format: "CSV",
                requesterUserId: 1001,
                sessionId: "2001"
            }
        });

        await qaService.listQaSources({ messageId: "4001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/source/list", {
            body: {
                messageId: "4001"
            }
        });

        await qaService.getQaTrace({ traceId: "9001" });
        expect(postJson).toHaveBeenLastCalledWith("/discovery/qa-admin/trace/get", {
            body: {
                traceId: "9001"
            }
        });

        const calledUrls = postJson.mock.calls.map(([url]) => String(url));
        expect(calledUrls.join("\n")).not.toMatch(/fastgpt|provider|dataset|collection/iu);
    });

    it("keeps qa admin service on Discovery APIs without provider direct urls", () => {
        const serviceSource = readFileSync(
            resolve(process.cwd(), "src/pages/discovery/qa-admin/qa-admin-service.ts"),
            "utf-8"
        );

        expect(serviceSource).toContain("/discovery/qa-admin/knowledge/health");
        expect(serviceSource).toContain("/discovery/qa-admin/knowledge/rebuild");
        expect(serviceSource).toContain("/discovery/qa-admin/knowledge/sync");
        expect(serviceSource).toContain("/discovery/qa-admin/knowledge/sync/page");
        expect(serviceSource).toContain("/discovery/qa-admin/session/delete");
        expect(serviceSource).toContain("/discovery/qa-admin/session/export");
        expect(serviceSource).not.toMatch(/https?:\/\/|fastgpt|dataset|collection|appId|baseUrl/iu);
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
